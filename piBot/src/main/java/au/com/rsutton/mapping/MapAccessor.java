package au.com.rsutton.mapping;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class MapAccessor
{
	private static final int MAX_HISTORY = 100;
	Map<XY, XY> map = new ConcurrentHashMap<XY, XY>();
	LinkedBlockingQueue<XY> history = new LinkedBlockingQueue<XY>();

	/**
	 * add this observation to all map locations that fit into the accuracy
	 * region
	 * 
	 * @param observation
	 */
	public void addObservation(Observation observation)
	{
		XY xy = new XY((int) observation.getX(), (int) observation.getY());
		XY previousObservation = map.get(xy);
		if (previousObservation == null)
		{
			map.put(xy, xy);
			history.add(xy);
		} else
		{
			previousObservation.increment();
			history.add(previousObservation);

		}
		if (history.size() > MAX_HISTORY)
		{
			try
			{
				XY old = history.take();
				old = map.get(old);
				if (old.decrement() == 0)
				{
					map.remove(old);
				}
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
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
	boolean isMapLocationClear(int x, int y, int spread)
	{
		
		int ctr = 0;
		for (int xl = x - spread; xl <= x + spread; xl++)
		{

			for (int yl = y - spread; yl <= y + spread; yl++)
			{
				XY xy = new XY(xl, yl);
				XY observation = map.get(xy);
				if (observation != null)
				{
					ctr+=observation.count;
				}
			}
		}

		return ctr < 2;
	}

	public Set<XY> getEntries()
	{
		return map.keySet();
	}
}
