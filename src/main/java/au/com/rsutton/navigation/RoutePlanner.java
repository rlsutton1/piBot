package au.com.rsutton.navigation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import au.com.rsutton.mapping.array.Dynamic2dSparseArray;
import au.com.rsutton.mapping.probability.ProbabilityMap;

public class RoutePlanner
{

	private ProbabilityMap world;

	int blockSize = 10;

	private Dynamic2dSparseArray route;

	public RoutePlanner(ProbabilityMap world)
	{
		this.world = world;
	}

	public static final class ExpansionPoint
	{
		public ExpansionPoint(int x2, int y2)
		{
			x = x2;
			y = y2;
		}

		@Override
		public String toString()
		{
			return x + "," + y;
		}

		final int x;
		final int y;

		public int getX()
		{
			return x;
		}

		public int getY()
		{
			return y;
		}
	}

	public void createRouteOld(int toX, int toY)
	{
		int wall = 10000;

		route = new Dynamic2dSparseArray(wall);

		int x = toX / blockSize;
		int y = toY / blockSize;
		int moveCounter = 0;

		List<ExpansionPoint> expansionPoints = new LinkedList<>();
		expansionPoints.add(new ExpansionPoint(x, y));
		route.set(x, y, moveCounter);
		while (!expansionPoints.isEmpty())
		{
			ExpansionPoint point = expansionPoints.remove(0);
			List<ExpansionPoint> tempPoints = new LinkedList<>();
			tempPoints.add(new ExpansionPoint(point.x + 1, point.y));
			tempPoints.add(new ExpansionPoint(point.x - 1, point.y));
			tempPoints.add(new ExpansionPoint(point.x, point.y - 1));
			tempPoints.add(new ExpansionPoint(point.x, point.y + 1));

			for (ExpansionPoint temp : tempPoints)
			{
				if (temp.x > world.getMinX() / blockSize && temp.x < world.getMaxX() / blockSize
						&& temp.y > world.getMinY() / blockSize && temp.y < world.getMaxY() / blockSize)
					if (world.get(temp.x * blockSize, temp.y * blockSize) < 0.55
							&& route.get(temp.x, temp.y) > moveCounter && route.get(temp.x, temp.y) == wall)
					{
						int penalty = 0;
						if (doWallCheck(temp, 2))
						{
							penalty = 2000;
						}

						route.set(temp.x, temp.y, moveCounter + penalty);
						expansionPoints.add(temp);
						moveCounter++;
					}
			}

		}
	}

	public void createRoute(int toX, int toY)
	{
		List<ExpansionPoint> immediatePoints = new LinkedList<>();
		List<ExpansionPoint> deferredPoints = new LinkedList<>();
		int wall = 10000;
		AtomicInteger moveCounter = new AtomicInteger();

		route = new Dynamic2dSparseArray(wall);

		int x = toX / blockSize;
		int y = toY / blockSize;

		immediatePoints.add(new ExpansionPoint(x, y));
		route.set(x, y, moveCounter.getAndIncrement());

		while (!immediatePoints.isEmpty() || !deferredPoints.isEmpty())
		{
			deferredPoints.addAll(expandPoints(immediatePoints, wall, moveCounter));
			immediatePoints.addAll(expandDeferredPoints(deferredPoints, moveCounter));
		}
	}

	/**
	 * 
	 * @param deferredPoints
	 * @return a list of immediatePoints
	 */
	private Collection<ExpansionPoint> expandDeferredPoints(List<ExpansionPoint> deferredPoints,
			AtomicInteger moveCounter)
	{
		List<ExpansionPoint> immediatePoints = new LinkedList<>();
		if (!deferredPoints.isEmpty())
		{
			ExpansionPoint temp = deferredPoints.remove(0);
			route.set(temp.x, temp.y, moveCounter.getAndIncrement());
			immediatePoints.add(temp);
		}
		return immediatePoints;
	}

	/**
	 * 
	 * @param immediatePoints
	 * @return a list of deferred points
	 */
	private Collection<ExpansionPoint> expandPoints(List<ExpansionPoint> immediatePoints, int wall,
			AtomicInteger moveCounter)
	{
		List<ExpansionPoint> deferredPoints = new LinkedList<>();

		while (!immediatePoints.isEmpty())
		{
			ExpansionPoint point = immediatePoints.remove(0);
			List<ExpansionPoint> tempPoints = new LinkedList<>();
			tempPoints.add(new ExpansionPoint(point.x + 1, point.y));
			tempPoints.add(new ExpansionPoint(point.x - 1, point.y));
			tempPoints.add(new ExpansionPoint(point.x, point.y - 1));
			tempPoints.add(new ExpansionPoint(point.x, point.y + 1));

			for (ExpansionPoint temp : tempPoints)
			{
				if (temp.x > world.getMinX() / blockSize && temp.x < world.getMaxX() / blockSize
						&& temp.y > world.getMinY() / blockSize && temp.y < world.getMaxY() / blockSize)
					if (world.get(temp.x * blockSize, temp.y * blockSize) < 0.55
							&& route.get(temp.x, temp.y) > moveCounter.get() && route.get(temp.x, temp.y) == wall)
					{

						if (doWallCheck(temp, 3))
						{
							deferredPoints.add(temp);
						} else
						{

							route.set(temp.x, temp.y, moveCounter.getAndIncrement());
							immediatePoints.add(temp);

						}
					}
			}

		}
		return deferredPoints;
	}

	boolean doWallCheck(ExpansionPoint point, int size)
	{

		for (int i = -size; i <= size; i++)
		{

			if (world.get((point.x + i) * blockSize, (point.y + i) * blockSize) > 0.5)
			{
				return true;
			}
		}
		return false;
	}

	public ExpansionPoint getRouteForLocation(int x, int y)
	{
		// System.out.println("Route from " + x + "," + y);
		int rx = x / blockSize;
		int ry = y / blockSize;

		List<ExpansionPoint> points = new LinkedList<>();
		points.add(new ExpansionPoint(1, 0));
		points.add(new ExpansionPoint(1, 1));
		points.add(new ExpansionPoint(0, 1));
		points.add(new ExpansionPoint(0, -1));
		points.add(new ExpansionPoint(-1, 0));
		points.add(new ExpansionPoint(-1, 1));
		points.add(new ExpansionPoint(-1, -1));

		ExpansionPoint target = new ExpansionPoint(x, y);
		double min = route.get(rx, ry);
		// System.out.println("pre min " + min + "rx,ry " + rx + "," + ry);
		for (ExpansionPoint point : points)
		{
			double value = route.get(rx + point.x, ry + point.y);
			if (value <= min)
			{
				min = value;
				target = new ExpansionPoint((x + point.x), (y + point.y));
			}
		}
		// System.out.println("min " + min);

		return target;
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
}
