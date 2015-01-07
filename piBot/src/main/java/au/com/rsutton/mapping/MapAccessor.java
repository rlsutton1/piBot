package au.com.rsutton.mapping;

import java.util.HashSet;
import java.util.Set;

import varunpant.Point;
import varunpant.QuadTree;

public class MapAccessor
{

	QuadTree qt = new QuadTree(-100000, -100000, 100000, 100000);

	/**
	 * add this observation to all map locations that fit into the accuracy
	 * region
	 * 
	 * @param observation
	 */
	public void addObservation(Observation observation)
	{
		qt.set(observation.getX(), observation.getY(),
				System.currentTimeMillis());

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

		Point[] result = qt.searchWithin(x - spread, y - spread, x + spread, y
				+ spread);
		return result.length < 2;
	}

	public Set<XY> getEntries()
	{
		Set<XY> points = new HashSet<>();
		for (Point point:qt.getKeys())
		{
			points.add(new XY((int)point.getX(),(int)point.getY()));
		}
		
		return points;
	}
}
