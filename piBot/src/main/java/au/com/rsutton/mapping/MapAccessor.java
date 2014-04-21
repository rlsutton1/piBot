package au.com.rsutton.mapping;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MapAccessor
{
	Map<XY, Observation> map = new ConcurrentHashMap<XY, Observation>();

	/**
	 * add this observation to all map locations that fit into the accuracy
	 * region
	 * 
	 * @param observation
	 */
	public void addObservation(Observation observation)
	{
		XY xy = new XY((int) observation.getX(), (int) observation.getY());
		Observation previousObservation = map.get(xy);
		if (previousObservation == null)
		{

			map.put(xy, observation);
		} else
		{
			if (previousObservation.getStatus() == observation.getStatus())
			{
				previousObservation.seenAgain();
			} else
			{
				map.put(xy, observation);
			}
		}

	}

	/**
	 * iterate all the observations for the given location and determine if the
	 * location is empty or occupied
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	LocationStatus isMapLocationClear(int x, int y, int spread)
	{
		LocationStatus status = LocationStatus.UNOBSERVED;

		for (int xl = x - spread; xl <= x + spread; xl++)
		{

			for (int yl = y - spread; yl <= y + spread; yl++)
			{
				XY xy = new XY(xl, yl);
				Observation observation = map.get(xy);
				if (observation != null)
				{
					status = observation.getStatus();
					if (status == LocationStatus.OCCUPIED)
					{
						break;
					}
				}
			}
			if (status == LocationStatus.OCCUPIED)
			{
				break;
			}
		}

		return status;
	}

	public Set<XY> getEntries()
	{
		return map.keySet();
	}
}
