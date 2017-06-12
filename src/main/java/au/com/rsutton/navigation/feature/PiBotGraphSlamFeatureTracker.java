package au.com.rsutton.navigation.feature;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.mapping.particleFilter.RobotPoseSource;
import au.com.rsutton.mapping.particleFilter.ScanObservation;
import au.com.rsutton.navigation.graphslam.v3.DimensionWrapperXYTheta;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.robot.rover.Angle;
import au.com.rsutton.robot.rover.AngleUnits;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.ui.MapDrawingWindow;

public class PiBotGraphSlamFeatureTracker implements SpikeListener, DataSourceMap, RobotPoseSource
{

	private Logger logger = LogManager.getLogger();
	private GraphSlamFeatureTracker tracker = new GraphSlamFeatureTracker();

	private double heading = 0;
	private Vector3D position = new Vector3D(0, 0, 0);
	private double lastX = 0;
	private double lastY = 0;
	private double lastHeading = 0;

	private boolean initial = true;

	public PiBotGraphSlamFeatureTracker(MapDrawingWindow ui, RobotInterface robot)
	{

		new FeatureExtractorSpike(this, robot);

		new FeatureExtractorCorner(this, robot);

		tracker.setNewLocation(0, 0, 0, 0.1);

		ui.addDataSource(this);

		setupRobotListener(robot);

	}

	void setupRobotListener(RobotInterface robot)
	{
		robot.addMessageListener(new RobotLocationDeltaListener()
		{

			@Override
			public void onMessage(Angle deltaHeading, Distance deltaDistance, List<ScanObservation> robotLocation)
			{
				logger.info("Delta heading " + deltaHeading.getDegrees());
				heading += deltaHeading.getDegrees();
				DistanceXY result = RobotLocationDeltaHelper.applyDelta(deltaHeading, deltaDistance,
						new Angle(heading, AngleUnits.DEGREES), new Distance(position.getX(), DistanceUnit.CM),
						new Distance(position.getY(), DistanceUnit.CM));

				position = result.getVector(DistanceUnit.CM);

				logger.error("Slam position " + deltaDistance + " " + position);
				updatePosition();
			}
		});

	}

	@Override
	public void discoveredSpikes(List<Feature> features)
	{

		updatePosition();
		if (!features.isEmpty())
			logger.error("Spikes found " + features.size());

		tracker.addObservations(features, heading);

	}

	private void updatePosition()
	{
		if (initial)
		{
			lastX = position.getX();
			lastY = position.getY();
			lastHeading = heading;
			initial = false;
		}

		double dx = position.getX() - lastX;
		double dy = position.getY() - lastY;
		double dt = HeadingHelper.getChangeInHeading(heading, lastHeading);
		if (Math.abs(dx) > 5 || Math.abs(dy) > 5 || Math.abs(dt) > 3)
		{
			lastX = position.getX();
			lastY = position.getY();
			lastHeading = heading;
			tracker.setNewLocation(dx, dy, dt, 0.9);
			logger.warn("Updating postion");
		}
	}

	@Override
	public List<Point> getPoints()
	{
		List<Point> points = new LinkedList<>();
		points.add(new Point(0, 0));
		return points;
	}

	@Override
	public void drawPoint(BufferedImage image, double pointOriginX, double pointOriginY, double scale)
	{
		Graphics graphics = image.getGraphics();
		//
		// DimensionWrapperXYTheta node = tracker.getCurrentLocation();
		// tracker.getCurrentConstraints
		//
		// draw all the current constraints
		//
		// draw lidar observation lines
		for (Feature feature : tracker.getFeatures())
		{
			graphics.setColor(new Color(255, 0, 0));
			if (feature.getFeatureType() == FeatureType.CONCAVE)
			{
				graphics.setColor(new Color(0, 255, 255));
			}
			int pointX = (int) (pointOriginX + (feature.x * scale));
			int pointY = (int) (pointOriginY + (feature.y * scale));
			graphics.drawRect(pointX, pointY, 5, 5);

			double direction = feature.angle + 90;

			Vector3D line1 = new Vector3D(0, 15, 0);
			line1 = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(direction)).applyTo(line1);

			graphics.drawLine(pointX, pointY, (int) (pointX + line1.getX()), (int) (pointY + line1.getY()));

			direction = feature.getAngleAwayFromWall() + 90;

			Vector3D line = new Vector3D(0, 8, 0);
			line = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(direction)).applyTo(line);

			graphics.setColor(new Color(255, 255, 0));

			graphics.drawLine((int) (pointX + line1.getX()), (int) (pointY + line1.getY()),
					(int) (pointX + line1.getX() + line.getX()), (int) (pointY + line1.getY() + line.getY()));

		}

	}

	@Override
	public double getHeading()
	{
		return heading;
	}

	@Override
	public DistanceXY getXyPosition()
	{
		DimensionWrapperXYTheta slamLocation = tracker.getCurrentLocation();
		return new DistanceXY(tracker.stablizedX, tracker.stablizedY, DistanceUnit.CM);
	}

	/**
	 * this is implemented by particle filter, generally return a low value
	 * (close to zero but not zero)
	 * 
	 * @return
	 */
	@Override
	public double getStdDev()
	{
		return 1;
	}

	@Override
	public void addDataSoures(MapDrawingWindow ui)
	{

	}

	@Override
	public void shutdown()
	{
	}

}
