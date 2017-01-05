package au.com.rsutton.navigation.router;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.mapping.array.Dynamic2dSparseArray;
import au.com.rsutton.mapping.particleFilter.Despecaler;
import au.com.rsutton.mapping.probability.ProbabilityMap;

public class RoutePlanner
{

	private ProbabilityMap world;

	static final int WALL = 1000000;

	int blockSize = 5;

	private Dynamic2dSparseArray route;

	private int targetX;

	private int targetY;

	public RoutePlanner(ProbabilityMap world)
	{
		this.world = world;
	}

	public void createRoute(int toX, int toY, RouteOption routeOption)
	{
		wallChecks.clear();

		Despecaler.despecal(world);

		this.targetX = toX;
		this.targetY = toY;

		List<ExpansionPoint> immediatePoints = new LinkedList<>();
		PrioritizedQueueGroup<ExpansionPoint> deferredPoints = new PrioritizedQueueGroup<>(6);
		AtomicInteger moveCounter = new AtomicInteger();

		route = new Dynamic2dSparseArray(WALL);

		int x = toX / blockSize;
		int y = toY / blockSize;

		immediatePoints.add(new ExpansionPoint(x, y));
		route.set(x, y, moveCounter.getAndIncrement());

		while (!immediatePoints.isEmpty() || !deferredPoints.isEmpty())
		{
			deferredPoints.addAll(expandPoints(immediatePoints, WALL, moveCounter, routeOption));
			immediatePoints.addAll(expandDeferredPoints(deferredPoints, moveCounter));
		}
	}

	/**
	 * 
	 * @param deferredPoints
	 * @return a list of immediatePoints
	 */
	private Collection<ExpansionPoint> expandDeferredPoints(PrioritizedQueueGroup<ExpansionPoint> deferredPoints,
			AtomicInteger moveCounter)
	{
		List<ExpansionPoint> immediatePoints = new LinkedList<>();
		if (!deferredPoints.isEmpty())
		{
			ExpansionPoint temp = deferredPoints.take();
			route.set(temp.x, temp.y, moveCounter.getAndIncrement());
			immediatePoints.add(temp);
		}
		return immediatePoints;
	}

	/**
	 * 
	 * @param imme
	 * @param routeOption
	 *            diatePoints
	 * @return a list of deferred points
	 */
	private PrioritizedQueueGroup<ExpansionPoint> expandPoints(List<ExpansionPoint> immediatePoints, int wall,
			AtomicInteger moveCounter, RouteOption routeOption)
	{
		PrioritizedQueueGroup<ExpansionPoint> deferredPoints = new PrioritizedQueueGroup<>(6);

		while (!immediatePoints.isEmpty())
		{
			ExpansionPoint point = immediatePoints.remove(0);
			List<ExpansionPoint> tempPoints = new LinkedList<>();
			tempPoints.add(new ExpansionPoint(point.x + 1, point.y));
			tempPoints.add(new ExpansionPoint(point.x - 1, point.y));
			tempPoints.add(new ExpansionPoint(point.x, point.y - 1));
			tempPoints.add(new ExpansionPoint(point.x, point.y + 1));
			tempPoints.add(new ExpansionPoint(point.x + 1, point.y + 1));
			tempPoints.add(new ExpansionPoint(point.x - 1, point.y - 1));
			tempPoints.add(new ExpansionPoint(point.x + 1, point.y - 1));
			tempPoints.add(new ExpansionPoint(point.x - 1, point.y + 1));

			for (ExpansionPoint temp : tempPoints)
			{
				if (isPointWithinWorldBountries(temp))
					if (routeOption.isPointRoutable(world.get(temp.x * blockSize, temp.y * blockSize))
							&& route.get(temp.x, temp.y) > moveCounter.get() && !isPointRoutedAlready(wall, temp))
					{

						int distanceToWall = doWallCheck(temp, 6);
						if (distanceToWall == -1)
						{
							route.set(temp.x, temp.y, moveCounter.getAndIncrement());
							immediatePoints.add(temp);
						} else
						{
							deferredPoints.add(distanceToWall, temp);
						}
					}
			}

		}
		return deferredPoints;
	}

	private boolean isPointRoutedAlready(int wall, ExpansionPoint temp)
	{
		return route.get(temp.x, temp.y) != wall;
	}

	private boolean isPointWithinWorldBountries(ExpansionPoint temp)
	{
		return temp.x > world.getMinX() / blockSize && temp.x < world.getMaxX() / blockSize
				&& temp.y > world.getMinY() / blockSize && temp.y < world.getMaxY() / blockSize;
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
			points.add(new ExpansionPoint(radius, 0));
			points.add(new ExpansionPoint(radius, radius));
			points.add(new ExpansionPoint(radius, -radius));
			points.add(new ExpansionPoint(0, radius));
			points.add(new ExpansionPoint(0, -radius));
			points.add(new ExpansionPoint(-radius, 0));
			points.add(new ExpansionPoint(-radius, radius));
			points.add(new ExpansionPoint(-radius, -radius));

			for (ExpansionPoint radiusPoint : points)
			{
				if (world.get((epoint.x + radiusPoint.x) * blockSize, (epoint.y + radiusPoint.y) * blockSize) > 0.5)
				{
					wallChecks.put(epoint, size);
					return size;
				}
			}
		}
		wallChecks.put(epoint, -1);
		return -1;
	}

	private ExpansionPoint getRouteForLocation(int x, int y, int radius)
	{
		// System.out.println("Route from " + x + "," + y);
		int rx = x / blockSize;
		int ry = y / blockSize;

		List<ExpansionPoint> points = new LinkedList<>();
		points.add(new ExpansionPoint(radius, 0));
		points.add(new ExpansionPoint(radius, radius));
		points.add(new ExpansionPoint(radius, -radius));
		points.add(new ExpansionPoint(0, radius));
		points.add(new ExpansionPoint(0, -radius));
		points.add(new ExpansionPoint(-radius, 0));
		points.add(new ExpansionPoint(-radius, radius));
		points.add(new ExpansionPoint(-radius, -radius));

		ExpansionPoint target = null;
		double min = route.get(rx, ry);
		// System.out.println("pre min " + min + "rx,ry " + rx + "," + ry);
		for (ExpansionPoint point : points)
		{
			double value = route.get(rx + point.x, ry + point.y);
			if (value < min)
			{
				min = value;
				target = new ExpansionPoint(x + point.x, y + point.y);
			}
		}
		// System.out.println("min " + min);
		if (min == WALL)
			return null;

		return target;
	}

	public ExpansionPoint getRouteForLocation(int x, int y)
	{
		ExpansionPoint result = null;

		for (int i = 1; i < blockSize * 2; i++)
		{
			result = getRouteForLocation(x, y, i);
			if (result != null)
			{
				return result;
			}
		}
		if (result == null)
		{
			result = new ExpansionPoint(x, y);
		}
		return result;
	}

	void dumpRoute()
	{
		for (int y = route.getMinY(); y < route.getMaxY(); y++)
		{
			for (int x = route.getMinX(); x < route.getMaxX(); x++)
			{
				System.out.print(route.get(x, y) + " ");
			}
			System.out.println("");
		}
	}

	/**
	 * searches the current route for the farthest point from the destination
	 * 
	 * @return
	 */
	public ExpansionPoint getFurthestPoint()
	{
		return null;

	}

	public boolean hasPlannedRoute()
	{
		return route != null;
	}

	public double getDistanceToTarget(int pfX, int pfY)
	{
		return new Vector3D(pfX - targetX, pfY - targetY, 0).getNorm();
	}
}
