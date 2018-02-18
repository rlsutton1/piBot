package au.com.rsutton.mapping;

import au.com.rsutton.mapping.probability.Occupancy;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;

public class MapAccessor
{

	ProbabilityMapIIFc qt = new ProbabilityMap(5);

	/**
	 * add this observation to all map locations that fit into the accuracy
	 * region
	 * 
	 * @param observation
	 */
	public void addObservation(Observation observation)
	{
		if (observation.getStatus() == LocationStatus.OCCUPIED)
		{
			qt.updatePoint((int) observation.getX(), (int) observation.getY(), Occupancy.OCCUPIED, 1, 1);
		} else
		{
			qt.updatePoint((int) observation.getX(), (int) observation.getY(), Occupancy.VACANT, 1, 1);

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
		return qt.get(x, y) <= 0.5;

	}

}
