package au.com.rsutton.navigation.router;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.hazelcast.LidarScan;
import au.com.rsutton.kalman.RobotPoseSourceTimeTraveling;
import au.com.rsutton.kalman.RobotPoseSourceTimeTraveling.RobotPoseInstant;
import au.com.rsutton.mapping.particleFilter.ScanObservation;
import au.com.rsutton.mapping.probability.Occupancy;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.navigation.feature.RobotLocationDeltaListener;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.ui.MapDrawingWindow;
import au.com.rsutton.ui.WrapperForObservedMapInMapUI;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;

public class MapStackerRaw implements RobotLocationDeltaListener, DataSourceMap
{

	private static final int BLOCK_SIZE = 5;

	private RobotPoseSourceTimeTraveling robotPoseSource;

	private ProbabilityMap world = new ProbabilityMap(BLOCK_SIZE);

	private MapDrawingWindow panel;

	Logger logger = LogManager.getLogger();

	public MapStackerRaw(RobotInterface robot, RobotPoseSourceTimeTraveling robotPoseSource)
	{
		world.setDefaultValue(0);
		this.robotPoseSource = robotPoseSource;
		robot.addMessageListener(this);
		panel = new MapDrawingWindow("MapStacker Raw", 600, 500, 1000, true);
		panel.addDataSource(new WrapperForObservedMapInMapUI(world));

	}

	@Override
	public void onMessage(LidarScan lidarScan)
	{

		// add points
		new Thread(() -> {

			try
			{
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			long start = lidarScan.getStartTime();
			long end = lidarScan.getEndTime();

			RobotPoseInstant poseSource1 = robotPoseSource.findInstant(start);
			RobotPoseInstant poseSource2 = robotPoseSource.findInstant(end);
			if (Math.abs(HeadingHelper.getChangeInHeading(poseSource1.getHeading(), poseSource2.getHeading())) > 1)
			{
				// too much rotation
				return;
			}

			RobotPoseInstant poseSource = robotPoseSource.findInstant(lidarScan.getStartTime());
			DistanceXY xy = poseSource.getXyPosition();

			double heading = poseSource.getHeading();

			for (ScanObservation observation : lidarScan.getObservations())
			{
				if (observation.getDisctanceCm() > 30)
				{

					Vector3D offset = new Vector3D(xy.getX().convert(DistanceUnit.CM),
							xy.getY().convert(DistanceUnit.CM), 0);

					Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(heading));

					Vector3D point = observation.getVector();
					Vector3D spot = rotation.applyTo(point).add(offset);
					world.updatePoint((int) spot.getX(), (int) spot.getY(), Occupancy.OCCUPIED, 1, 1);
				}
			}

		}).start();

	}

	void test()
	{
		LidarScan lidarScan = null;
		for (ScanObservation observation : lidarScan.getObservations())
		{
			if (observation.getDisctanceCm() > 30)
			{

				int end = 0;
				int start = 0;
				long duration = end - start;

				double s = observation.getAngleRadians() / (2.0 * Math.PI);

				long time = (long) (start + (duration * s));

				RobotPoseInstant poseSource = robotPoseSource.findInstant(time);
				DistanceXY xy = poseSource.getXyPosition();
				double heading = poseSource.getHeading();

				Vector3D offset = new Vector3D(xy.getX().convert(DistanceUnit.CM), xy.getY().convert(DistanceUnit.CM),
						0);

				Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(heading));

				Vector3D point = observation.getVector();
				Vector3D spot = rotation.applyTo(point).add(offset);
				world.updatePoint((int) spot.getX(), (int) spot.getY(), Occupancy.OCCUPIED, 1, 1);
			}
		}
	}

	@Override
	public List<Point> getPoints()
	{
		List<Point> points = new LinkedList<>();
		for (int y = world.getMinY() - 30; y <= world.getMaxY() + 30; y += 3)
		{
			for (int x = world.getMinX() - 30; x <= world.getMaxX() + 30; x += 3)
			{
				Point point = new Point(x, y);

				points.add(point);

			}
		}
		return points;
	}

	@Override
	public void drawPoint(BufferedImage image, double pointOriginX, double pointOriginY, double scale, double originalX,
			double originalY)
	{

		Graphics graphics = image.getGraphics();

		graphics.setColor(Color.ORANGE);

		graphics.drawLine((int) (pointOriginX), (int) (pointOriginY), (int) ((pointOriginX + 1)),
				(int) ((pointOriginY + 1)));
		// System.out.println(pointOriginX + " " + pointOriginY + " " + value);

	}

	@Override
	public void onMessage(Angle deltaHeading, Distance deltaDistance, boolean bump)
	{
		// these messages are not needed here
	}

}
