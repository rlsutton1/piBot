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

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.mapping.particleFilter.ParticleFilterIfc;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.ui.MapDrawingWindow;

public class PiBotGraphSlamFeatureTracker implements SpikeListener, DataSourceMap
{

	private FeatureExtractorSpike spikeExtractor;
	private FeatureExtractorCorner cornerExtractor;

	GraphSlamFeatureTracker tracker = new GraphSlamFeatureTracker();
	private ParticleFilterIfc pf;

	public PiBotGraphSlamFeatureTracker(MapDrawingWindow ui, ParticleFilterIfc pf, RobotInterface robot)
	{

		this.pf = pf;

		spikeExtractor = new FeatureExtractorSpike(this, robot);
		// ui.addDataSource(spikeExtractor.getHeadingMapDataSource(pf, robot));

		cornerExtractor = new FeatureExtractorCorner(this, robot);
		// ui.addDataSource(cornerExtractor.getHeadingMapDataSource(pf, robot));

		ui.addDataSource(this);

	}

	double lastX = 0;
	double lastY = 0;
	double lastHeading = 0;

	@Override
	public void discoveredSpikes(List<Spike> spikes)
	{
		if (pf.getStdDev() < 60)
		{
			double heading = pf.getAverageHeading();
			Vector3D position = pf.dumpAveragePosition();

			double dx = position.getX() - lastX;
			double dy = position.getY() - lastY;
			double dt = HeadingHelper.getChangeInHeading(heading, lastHeading);
			if (Math.abs(dx) > 1 || Math.abs(dy) > 1)// || Math.abs(dt) > 3)
			{
				lastX = position.getX();
				lastY = position.getY();
				lastHeading = heading;
				tracker.setNewLocation(dx, dy, 0, 0.25);
			}

			double angleOffset = 0;

			for (Spike spike : spikes)
			{

				Vector3D spd = new Vector3D(spike.x, spike.y, 0);
				Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(heading));

				Vector3D result = rotation.applyTo(spd);

				Spike offsetSpike = new Spike(result.getX(), result.getY(), heading + spike.angle + angleOffset,
						heading + spike.getAngleAwayFromWall() + angleOffset);
				tracker.addObservation(offsetSpike);
			}

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

		// draw lidar observation lines
		for (Spike spike : tracker.featureMap.values())
		// for (Spike spike : current)

		{
			graphics.setColor(new Color(255, 0, 0));

			int pointX = (int) (pointOriginX + (spike.x * scale));
			int pointY = (int) (pointOriginY + (spike.y * scale));
			graphics.drawRect(pointX, pointY, 5, 5);

			double direction = spike.angle + 90;

			Vector3D line1 = new Vector3D(0, 30, 0);
			line1 = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(direction)).applyTo(line1);

			graphics.drawLine(pointX, pointY, (int) (pointX + line1.getX()), (int) (pointY + line1.getY()));

			direction = spike.getAngleAwayFromWall() + 90;

			Vector3D line = new Vector3D(0, 15, 0);
			line = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(direction)).applyTo(line);

			graphics.setColor(new Color(255, 255, 0));
			graphics.drawLine((int) (pointX + line1.getX()), (int) (pointY + line1.getY()),
					(int) (pointX + line1.getX() + line.getX()), (int) (pointY + line1.getY() + line.getY()));

		}

	}

}
