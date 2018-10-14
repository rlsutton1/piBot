package au.com.rsutton.navigation.router;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.logging.log4j.LogManager;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.RateLimiter;

import au.com.rsutton.mapping.particleFilter.RobotPoseSource;
import au.com.rsutton.mapping.particleFilter.ScanObservation;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.navigation.feature.RobotLocationDeltaListener;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;

public class RoutePlannerLastMeter implements RoutePlanner, RobotLocationDeltaListener
{

	private static final int BLOCK_SIZE = 5;

	private final RoutePlanner basePlanner;

	private final AtomicReference<RoutePlanner> localPlanner = new AtomicReference<>();

	private RobotPoseSource robotPoseSource;

	private ProbabilityMapIIFc world;

	public RoutePlannerLastMeter(ProbabilityMapIIFc world, RobotInterface robot, RobotPoseSource robotPoseSource)
	{
		basePlanner = new RoutePlannerImpl(world);
		this.world = world;
		this.robotPoseSource = robotPoseSource;
		robot.addMessageListener(this);

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
		return new ExpansionPoint(x, y, 0);
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
		return basePlanner.hasPlannedRoute();
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
			if (!rateLimiter.tryAcquire() || !basePlanner.hasPlannedRoute())
			{
				singlePass.release();
				return;
			}

			new Thread(() -> worker(observations, 100)).start();
		}
	}

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

		// overlay the current incoming lidar scan

		Vector3D offset = new Vector3D(pos.getX().convert(DistanceUnit.CM), pos.getY().convert(DistanceUnit.CM), 0);
		Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(heading));
		for (ScanObservation obs : observations)
		{
			if (obs.getDisctanceCm() > 40)
			{
				Vector3D point = obs.getVector();
				Vector3D spot = rotation.applyTo(point).add(offset);
				localMap.writeRadius((int) spot.getX(), (int) spot.getY(), 1, 1);
			}
		}

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
			}
		}
		singlePass.release();
	}

}
