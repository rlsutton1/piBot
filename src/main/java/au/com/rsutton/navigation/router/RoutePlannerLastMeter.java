package au.com.rsutton.navigation.router;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.RateLimiter;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import au.com.rsutton.hazelcast.PointCloudMessage;
import au.com.rsutton.mapping.particleFilter.RobotPoseSource;
import au.com.rsutton.mapping.particleFilter.ScanObservation;
import au.com.rsutton.mapping.probability.Occupancy;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.navigation.feature.RobotLocationDeltaListener;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;

public class RoutePlannerLastMeter implements RoutePlanner, RobotLocationDeltaListener, DataSourceMap
{

	private static final int BLOCK_SIZE = 5;

	private final RoutePlanner basePlanner;

	private final AtomicReference<RoutePlanner> localPlanner = new AtomicReference<>();

	private RobotPoseSource robotPoseSource;

	private ProbabilityMapIIFc world;

	private final ProbabilityMap worldFromPointCloud = new ProbabilityMap(5);

	Logger logger = LogManager.getLogger();

	private final AtomicReference<PointCloudWrapper> lastPointCloudMessage = new AtomicReference<>();

	public RoutePlannerLastMeter(ProbabilityMapIIFc world, RobotInterface robot, RobotPoseSource robotPoseSource)
	{
		basePlanner = new RoutePlannerImpl(world);
		this.world = world;
		this.robotPoseSource = robotPoseSource;
		robot.addMessageListener(this);

		new PointCloudMessage().addMessageListener(getPointCloudMessageListener());

	}

	class PointCloudWrapper
	{
		private PointCloudMessage message;
		private DistanceXY xy;
		private double heading;

		PointCloudWrapper(PointCloudMessage message)
		{
			this.message = message;
			this.xy = robotPoseSource.getXyPosition();
			this.heading = robotPoseSource.getHeading();
		}
	}

	private MessageListener<PointCloudMessage> getPointCloudMessageListener()
	{
		return new MessageListener<PointCloudMessage>()
		{

			@Override
			public void onMessage(Message<PointCloudMessage> message)
			{
				lastPointCloudMessage.set(new PointCloudWrapper(message.getMessageObject()));

			}
		};
	}

	@Override
	public boolean createRoute(int toX, int toY, RouteOption routeOption)
	{
		localPlanner.set(null);
		return basePlanner.createRoute(toX, toY, routeOption);
	}

	@Override
	public ExpansionPoint getRouteForLocation(int x, int y)
	{
		RoutePlanner lp = localPlanner.get();
		if (lp != null)
		{
			ExpansionPoint result = lp.getRouteForLocation(x, y);
			if (result.getX() != x || result.getY() != y)
			{
				return result;
			}
		}
		return new ExpansionPoint(x, y, 0, null);
	}

	public ExpansionPoint getMasterRouteForLocation(int x, int y)
	{
		return basePlanner.getRouteForLocation(x, y);
	}

	@Override
	public boolean hasPlannedRoute()
	{
		RoutePlanner lp = localPlanner.get();
		if (lp != null)
		{
			return lp.hasPlannedRoute();
		}
		return false;
	}

	@Override
	public double getDistanceToTarget(int pfX, int pfY)
	{
		RoutePlanner lp = localPlanner.get();
		if (lp != null)
		{
			return lp.getDistanceToTarget(pfX, pfY);
		}
		return basePlanner.getDistanceToTarget(pfX, pfY);
	}

	RateLimiter rateLimiter = RateLimiter.create(1);
	Semaphore singlePass = new Semaphore(1);

	@Override
	public void onMessage(Angle deltaHeading, Distance deltaDistance, List<ScanObservation> observations, boolean bump)
	{

		if (singlePass.tryAcquire())
		{
			if (lastPointCloudMessage.get() != null)
			{
				final DistanceXY pos = lastPointCloudMessage.get().xy;
				final double heading = lastPointCloudMessage.get().heading;

				final Vector3D offset = new Vector3D(pos.getX().convert(DistanceUnit.CM),
						pos.getY().convert(DistanceUnit.CM), 0);

				int minDistance = 50;
				int maxDistance = 150;
				int step = 5;
				// clear
				for (int clearHeading = -30; clearHeading < 30; clearHeading++)
				{
					Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(heading + clearHeading));
					Vector3D direction = rotation.applyTo(new Vector3D(0, step, 0));
					Vector3D clear = offset;

					for (int d = minDistance; d < maxDistance + 10; d += step)
					{
						clear = clear.add(direction);
						// worldFromPointCloud.resetPoint((int) clear.getX(),
						// (int) clear.getY());
						worldFromPointCloud.updatePoint((int) clear.getX(), (int) clear.getY(), Occupancy.VACANT, .005,
								1);

					}

				}

				// add points

				Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(heading));
				for (Vector3D observation : lastPointCloudMessage.get().message.getPoints())
				{
					if (observation.getNorm() < maxDistance)
					{
						Vector3D point = observation;
						Vector3D spot = rotation.applyTo(point).add(offset);
						worldFromPointCloud.updatePoint((int) spot.getX(), (int) spot.getY(), Occupancy.OCCUPIED, .40,
								3);
						// worldFromPointCloud.writeRadius((int) spot.getX(),
						// (int) spot.getY(), 1, 1);
					}
				}
			}
			if (!rateLimiter.tryAcquire() || !basePlanner.hasPlannedRoute())
			{
				singlePass.release();
				return;
			}

			new Thread(() -> worker(observations, 150)).start();
		}
	}

	double EXISTANCE_THRESHOLD = 0.7;

	private void worker(final List<ScanObservation> observations, int radius)
	{

		Stopwatch timer = Stopwatch.createStarted();

		DistanceXY pos = robotPoseSource.getXyPosition();
		double heading = robotPoseSource.getHeading();

		ProbabilityMap localMap = new ProbabilityMap(BLOCK_SIZE);

		double x = pos.getX().convert(DistanceUnit.CM);
		double y = pos.getY().convert(DistanceUnit.CM);

		// copy a radius from the world map into the local map
		for (int dx = -radius; dx < radius; dx++)
		{
			for (int dy = -radius; dy < radius; dy++)
			{
				localMap.writeRadius((int) (x + dx), (int) (y + dy), world.get(x + dx, y + dy), 1);
			}
		}

		// copy a radius from the worldFromPointCloud map into the local map
		for (int dx = -radius; dx < radius; dx++)
		{
			for (int dy = -radius; dy < radius; dy++)
			{
				if (worldFromPointCloud.get(x + dx, y + dy) > EXISTANCE_THRESHOLD)
				{
					localMap.writeRadius((int) (x + dx), (int) (y + dy), 1, 1);
				}
			}
		}

		// overlay the current incoming lidar scan

		Vector3D offset = new Vector3D(pos.getX().convert(DistanceUnit.CM), pos.getY().convert(DistanceUnit.CM), 0);
		Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(heading));
		for (ScanObservation obs : observations)
		{
			if (obs.getDisctanceCm() < 50 && Math.toDegrees(obs.getAngleRadians()) > 160
					&& Math.toDegrees(obs.getAngleRadians()) < 200)
			{
				// ignore some dodgy points coming from the LIDAR close by,
				// immediately behind the robot
			} else
			{
				Vector3D point = obs.getVector();
				Vector3D spot = rotation.applyTo(point).add(offset);
				localMap.writeRadius((int) spot.getX(), (int) spot.getY(), 1, 1);
			}
		}

		// // overlay point cloud from depth camera
		// PointCloudMessage pointCloud = lastPointCloudMessage.get();
		// if (pointCloud != null && pointCloud.getTime() >
		// System.currentTimeMillis() - 200L)
		// {
		// for (Vector3D obs : pointCloud.getPoints())
		// {
		// if (obs.getNorm() < 30)
		// {
		// // ignore some dodgy points coming from close by,
		// // immediately behind the robot
		// } else
		// {
		//
		// Vector3D point = obs;
		// Vector3D spot = rotation.applyTo(point).add(offset);
		// localMap.writeRadius((int) spot.getX(), (int) spot.getY(), 1, 1);
		// }
		// }
		// }

		// look ahead 100 CM

		ExpansionPoint next = basePlanner.getRouteForLocation((int) x, (int) y);
		for (int i = 0; i < radius - 10; i++)
			next = basePlanner.getRouteForLocation(next.getX(), next.getY());

		// plan the route
		RoutePlannerImpl newLocalPlanner = new RoutePlannerImpl(localMap);
		boolean success = newLocalPlanner.createRoute(next.getX(), next.getY(),
				RouteOption.ROUTE_THROUGH_CLEAR_SPACE_ONLY);

		if (success)
		{
			localPlanner.set(newLocalPlanner);

			LogManager.getLogger()
					.error("Local Route plan took " + timer.elapsed(TimeUnit.MILLISECONDS) + "ms for radius " + radius);

		} else
		{
			if (radius < 300)
			{
				LogManager.getLogger().error("Failed to create route, trying larger radius");
				worker(observations, radius + 50);
			} else
			{
				try
				{
					logger.error("Unable to plan a route!!!");
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		singlePass.release();
	}

	@Override
	public List<Point> getPoints()
	{
		List<Point> points = new LinkedList<>();
		for (int y = worldFromPointCloud.getMinY() - 30; y <= worldFromPointCloud.getMaxY() + 30; y += 3)
		{
			for (int x = worldFromPointCloud.getMinX() - 30; x <= worldFromPointCloud.getMaxX() + 30; x += 3)
			{
				Point point = new Point(x, y);
				if (worldFromPointCloud.get(x, y) > EXISTANCE_THRESHOLD)
				{
					points.add(point);
				}
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

}
