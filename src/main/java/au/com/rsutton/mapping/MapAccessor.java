package au.com.rsutton.mapping;

import java.awt.Rectangle;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import varunpant.Point;
import varunpant.QuadTree;

public class MapAccessor
{

	QuadTree<Observation> qt = new QuadTree<Observation>(-100000, -100000, 100000, 100000);

	/**
	 * add this observation to all map locations that fit into the accuracy
	 * region
	 * 
	 * @param observation
	 */
	public void addObservation(Observation observation)
	{
		qt.set(observation.getX(), observation.getY(),
				observation);

	}

	/**
	 * iterate all the observations for the given location and determine if the
	 * location is empty or occupied
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	boolean isMapLocationClear(int x, int y, int spread)
	{

		Point<Observation>[] result = qt.searchWithin(x - spread, y - spread, x + spread, y
				+ spread);
		return result.length < 1;
	}

	public Set<XY> getEntries()
	{
		Set<XY> points = new HashSet<>();
		for (Point point : qt.getKeys())
		{
			points.add(new XY((int) point.getX(), (int) point.getY()));
		}

		return points;
	}

	public List<Observation> getPointsInRange(Rectangle rectangle)
	{
		Point<Observation>[] points = qt.searchWithin(rectangle.getMinX(),
				rectangle.getMinY(), rectangle.getMaxX(), rectangle.getMaxY());

		List<Observation> results = new LinkedList<>();
		for (Point<Observation> point : points)
		{
			results.add(point.getValue());
		}
		return results;
	}
}
