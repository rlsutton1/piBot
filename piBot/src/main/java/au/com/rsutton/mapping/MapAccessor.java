package au.com.rsutton.mapping;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MapAccessor
{
	Map<XY, List<Observation>> map = new ConcurrentHashMap<XY, List<Observation>>();

	/**
	 * add this observation to all map locations that fit into the accuracy
	 * region
	 * 
	 * @param observation
	 */
	public void addObservation(Observation observation)
	{
		int minX = (int) (observation.getX() - 5);
		int maxX = (int) (observation.getX() + 5);

		int minY = (int) (observation.getY() - 5);
		int maxY = (int) (observation.getY() + 5);

		for (int y = minY; y <= maxY; y++)
		{
			for (int x = minX; x <= maxX; x++)
			{
				XY xy = new XY(x, y);
				List<Observation> observations = map.get(xy);
				if (observations == null)
				{
					observations = new LinkedList<Observation>();
					map.put(xy, observations);
				}
				observations.add(observation);
			}
		}

	}

	/**
	 * check if there is any data for the given location
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	boolean hasMapLocationBeenObserved(int x, int y)
	{
		XY xy = new XY(x, y);
		return map.get(xy) != null;
	}

	/**
	 * iterate all the observations for the given location and determine if the
	 * location is empty or occupied
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	boolean isMapLocationClear(int x, int y)
	{
		XY xy = new XY(x, y);
		List<Observation> observations = map.get(xy);
		double status = 0.5;
		double count = 1;
		double allowableSpread = 5.0d;
		for (Observation observation : observations)
		{
			double deltaXY = (Math.abs(x - observation.getX()) + Math.abs(y
					- observation.getY()));
			if (deltaXY < allowableSpread)
			{
				if (observation.isObject())
				{
					// values between 0.5 and 1.0
					status += ((allowableSpread - deltaXY) / (allowableSpread * 2.0d)) + 0.5d;
				} else
				{
					status -= ((allowableSpread - deltaXY) / (allowableSpread * 2.0d)) + 0.5d;
				}
				count++;
			}
		}
		status = status / count;

		return status < 0.5;
	}

	public Set<XY> getEntries()
	{
		return map.keySet();
	}
}
