package au.com.rsutton.navigation.router;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.logging.log4j.LogManager;

import com.google.common.base.Stopwatch;

import au.com.rsutton.mapping.array.Dynamic2dSparseArray;
import au.com.rsutton.mapping.array.SparseArray;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.ui.DataSourceMap;

public class RoutePlannerImpl implements RoutePlanner
{

	private ProbabilityMapIIFc sourceMap;

	private ProbabilityMapIIFc augmentedMap;

	static final int WALL = Integer.MAX_VALUE;

	int blockSize = 5;

	private SparseArray<RouteDataPoint> route;

	private int targetX;

	private int targetY;

	private int minimumClearance = 5;

	private int desiredClearance = 25;

	private int robotRadius = 15;

	class RouteDataPoint
	{
		public RouteDataPoint(int totalCost)
		{
			this.totalCost = totalCost;
		}

		int totalCost;

		public boolean isTotalCostLessThan(RouteDataPoint min)
		{
			return totalCost < min.totalCost;
		}
	}

	public RoutePlannerImpl(ProbabilityMapIIFc world)
	{
		this.sourceMap = world;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see au.com.rsutton.navigation.router.Route#createRoute(int, int,
	 * au.com.rsutton.navigation.router.RouteOption)
	 */
	@Override
	public boolean createRoute(int toX, int toY, RouteOption routeOption)
	{
		Stopwatch timer = Stopwatch.createStarted();
		wallChecks.clear();

		augmentedMap = createAugmentedMap(sourceMap);

		if (!routeOption.isPointRoutable(augmentedMap.get(toX, toY)))
		{
			return false;
		}

		// Despecaler.despecal(augmentedMap);

		this.targetX = toX;
		this.targetY = toY;

		PriorityBlockingQueue<ExpansionPoint> immediatePoints = new PriorityBlockingQueue<>();

		route = new Dynamic2dSparseArray<>(new RouteDataPoint(WALL));

		int x = toX / blockSize;
		int y = toY / blockSize;

		immediatePoints.add(new ExpansionPoint(x, y, 0, null));

		while (!immediatePoints.isEmpty())
		{
			ExpansionPoint point;
			try
			{
				point = immediatePoints.take();
				immediatePoints.addAll(expandPoints(point, routeOption));
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		// dumpRoute();

		LogManager.getLogger().error("Route plan took " + timer.elapsed(TimeUnit.MILLISECONDS) + "ms");

		return true;

	}

	/**
	 * build augmented map with all obsticles increased in size to that of the
	 * radius of the robot+ the minimum required clearance, thus preventing
	 * navigation to areas that would cause a collision
	 * 
	 * @param source
	 * @return
	 */
	ProbabilityMapIIFc createAugmentedMap(ProbabilityMapIIFc source)
	{
		ProbabilityMap matchMap = new ProbabilityMap(5);
		matchMap.setDefaultValue(0.5);
		matchMap.erase();
		int radius = robotRadius + minimumClearance;

		int minX = source.getMinX();
		int maxX = source.getMaxX();
		int minY = source.getMinY();
		int maxY = source.getMaxY();
		matchMap.writeRadius(minX, minY, 1, 1);
		matchMap.writeRadius(minX, maxY, 1, 1);
		matchMap.writeRadius(maxX, maxY, 1, 1);
		matchMap.writeRadius(maxX, minY, 1, 1);
		matchMap.convertToDenseOffsetArray();

		for (int x = minX; x < maxX + 1; x++)
		{
			for (int y = minY; y < maxY + 1; y++)
			{
				double value = source.get(x, y);
				if (value < 0.5)
				{
					matchMap.writeRadius(x, y, 0, robotRadius);

				}
			}
		}

		for (int x = minX; x < maxX + 1; x++)
		{
			for (int y = minY; y < maxY + 1; y++)
			{
				double value = source.get(x, y);
				if (value > 0.5)
				{
					matchMap.writeRadius(x, y, 1, radius);
				}
			}
		}

		return matchMap;
	}

	/**
	 * 
	 * @param routeOption
	 *            diatePoints
	 * @param imme
	 * @return a list of deferred points
	 */
	private List<ExpansionPoint> expandPoints(ExpansionPoint point, RouteOption routeOption)
	{
		List<ExpansionPoint> tempPoints = new LinkedList<>();

		// add expands to return list

		rateAndCreatePoint(tempPoints, point.x + 1, point.y, routeOption, point.getTotalCost(), point);
		rateAndCreatePoint(tempPoints, point.x - 1, point.y, routeOption, point.getTotalCost(), point);
		rateAndCreatePoint(tempPoints, point.x, point.y - 1, routeOption, point.getTotalCost(), point);
		rateAndCreatePoint(tempPoints, point.x, point.y + 1, routeOption, point.getTotalCost(), point);
		rateAndCreatePoint(tempPoints, point.x + 1, point.y + 1, routeOption, point.getTotalCost(), point);
		rateAndCreatePoint(tempPoints, point.x - 1, point.y - 1, routeOption, point.getTotalCost(), point);
		rateAndCreatePoint(tempPoints, point.x + 1, point.y - 1, routeOption, point.getTotalCost(), point);
		rateAndCreatePoint(tempPoints, point.x - 1, point.y + 1, routeOption, point.getTotalCost(), point);

		return tempPoints;
	}

	private void rateAndCreatePoint(List<ExpansionPoint> tempPoints, int x, int y, RouteOption routeOption,
			double distanceToTarget, ExpansionPoint parentPoint)
	{

		ExpansionPoint temp = new ExpansionPoint(x, y, 0, parentPoint);
		if (isPointWithinWorldBountries(temp))
			if (routeOption.isPointRoutable(augmentedMap.get(temp.x * blockSize, temp.y * blockSize))
					&& route.get(temp.x, temp.y).totalCost > distanceToTarget)
			{

				// radius in blocks
				int radius = (robotRadius + desiredClearance) / blockSize;

				double distanceToWall = doWallCheck(temp, radius);
				int penalty = (int) ((radius - distanceToWall) * 5000);

				penalty += getTurnCost(temp);

				int totalCost = (int) (distanceToTarget + 1 + penalty);
				if (route.get(temp.x, temp.y).totalCost > totalCost)
				{
					route.set(temp.x, temp.y, new RouteDataPoint(totalCost));

					ExpansionPoint expansionPoint = new ExpansionPoint(x, y, totalCost, parentPoint);
					expansionPoint.setAngularVelocity(temp.getPathAngularVelocity());
					expansionPoint.setPathAngle(temp.getPathAngle());
					tempPoints.add(expansionPoint);
				}
			}

	}

	private int getTurnCost(ExpansionPoint point)
	{

		ExpansionPoint child = point;
		double parentPathAngle = 0;
		double parentPathAngularVelocity = 0;
		ExpansionPoint parent = point.getParent();
		double angleFromParentToChild = 0;

		if (parent == null || parent.getPathAngle() == null || parent.getPathAngularVelocity() == null)
		{
		} else
		{
			parentPathAngle = parent.getPathAngle();
			parentPathAngularVelocity = parent.getPathAngularVelocity();
			angleFromParentToChild = Math.toDegrees(new Vector3D(child.x - parent.x, child.y - parent.y, 0).getNorm());
		}
		// calculate cost of child for parent

		double expectedAngle = parentPathAngle + parentPathAngularVelocity;
		double da = angleFromParentToChild - expectedAngle;

		double accel = 1.5;
		double decel = 0.9;
		double newAngularVelocity = (parentPathAngularVelocity + (da * accel)) * decel;

		child.setPathAngle(parentPathAngle + newAngularVelocity);
		child.setAngularVelocity(newAngularVelocity);

		double costScalar = 4;

		// return 1;
		int abs = (int) Math.abs((da * costScalar));
		// if (abs > 10)
		// {
		// System.out.println("Abs value " + abs);
		// }
		// return abs;
		return 0;

	}

	private boolean isPointWithinWorldBountries(ExpansionPoint temp)
	{
		return temp.x > augmentedMap.getMinX() / blockSize && temp.x < augmentedMap.getMaxX() / blockSize
				&& temp.y > augmentedMap.getMinY() / blockSize && temp.y < augmentedMap.getMaxY() / blockSize;
	}

	Map<ExpansionPoint, Integer> wallChecks = new HashMap<>();

	/**
	 * 
	 * @param point
	 * @param size
	 *            - distance to look for walls
	 * @return the distance to the nearest wall, or -1 if the wall is further
	 *         than size away
	 */
	int doWallCheck(ExpansionPoint epoint, int size)
	{
		if (wallChecks.get(epoint) != null)
		{
			return wallChecks.get(epoint);
		}

		for (int radius = 1; radius <= size; radius++)
		{
			List<ExpansionPoint> points = new LinkedList<>();
			for (int p = -radius; p <= radius; p++)
			{
				points.add(new ExpansionPoint(p, -radius, 0, epoint));
				points.add(new ExpansionPoint(p, radius, 0, epoint));
				points.add(new ExpansionPoint(-radius, p, 0, epoint));
				points.add(new ExpansionPoint(radius, p, 0, epoint));

			}

			for (ExpansionPoint radiusPoint : points)
			{
				if (augmentedMap.get((epoint.x + radiusPoint.x) * blockSize,
						(epoint.y + radiusPoint.y) * blockSize) > 0.5)
				{
					wallChecks.put(epoint, radius);
					return radius;
				}
			}
		}
		wallChecks.put(epoint, size);
		return size;
	}

	int getValueAtLocation(int x, int y)
	{
		int rx = x / blockSize;
		int ry = y / blockSize;
		return route.get(rx, ry).totalCost;
	}

	private ExpansionPoint getRouteForLocation(int x, int y, int radius)
	{
		// System.out.println("Route from " + x + "," + y);
		int rx = x / blockSize;
		int ry = y / blockSize;

		List<ExpansionPoint> points = new LinkedList<>();
		points.add(new ExpansionPoint(radius, 0, 0, null));
		points.add(new ExpansionPoint(radius, radius, 0, null));
		points.add(new ExpansionPoint(radius, -radius, 0, null));
		points.add(new ExpansionPoint(0, radius, 0, null));
		points.add(new ExpansionPoint(0, -radius, 0, null));
		points.add(new ExpansionPoint(-radius, 0, 0, null));
		points.add(new ExpansionPoint(-radius, radius, 0, null));
		points.add(new ExpansionPoint(-radius, -radius, 0, null));

		ExpansionPoint target = null;
		RouteDataPoint min = route.get(rx, ry);
		// System.out.println("pre min " + min + "rx,ry " + rx + "," + ry);
		for (ExpansionPoint point : points)
		{
			RouteDataPoint value = route.get(rx + point.x, ry + point.y);
			if (value.isTotalCostLessThan(min))
			{
				min = value;
				target = new ExpansionPoint(x + point.x, y + point.y, 0, null);
			}
		}

		// System.out.println("min " + min);

		return target;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see au.com.rsutton.navigation.router.Route#getRouteForLocation(int, int)
	 */
	@Override
	public ExpansionPoint getRouteForLocation(int x, int y)
	{
		ExpansionPoint result = null;

		for (int i = 1; i < blockSize * 2; i++)
		{
			result = getRouteForLocation(x, y, i);
			if (result != null)
			{
				break;
			}
		}
		if (result == null)
		{
			result = new ExpansionPoint(x, y, 0, null);
		}
		return result;
	}

	void dumpRoute()
	{
		System.out.println("\nStart route dump");
		for (int y = route.getMinY(); y < route.getMaxY(); y++)
		{
			for (int x = route.getMinX(); x < route.getMaxX(); x++)
			{
				System.out.print(route.get(x, y).totalCost + " ");
			}
			System.out.println("");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see au.com.rsutton.navigation.router.Route#hasPlannedRoute()
	 */
	@Override
	public boolean hasPlannedRoute()
	{
		return route != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see au.com.rsutton.navigation.router.Route#getDistanceToTarget(int, int)
	 */
	@Override
	public double getDistanceToTarget(int pfX, int pfY)
	{
		return new Vector3D(pfX - targetX, pfY - targetY, 0).getNorm();
	}

	@Override
	public DataSourceMap getGdPointSource()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
