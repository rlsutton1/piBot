package au.com.rsutton.calabrate;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public abstract class RandomPairSelection<T>
{

	interface Pair<T>
	{

	}

	/**
	 * 
	 * @param data
	 * @return - true if evaluation was successful
	 */
	boolean evaluateDataCombinations(List<T> data)
	{
		List<T> subset = new LinkedList<>();
		subset.addAll(data);

		List<Pair<T>> workingList = new LinkedList<>();

		Random rand = new Random();

		while (subset.size() > 0)
		{
			T v2 = subset.remove(rand.nextInt(subset.size()));
			for (T v1 : subset)
			{
				workingList.add(createPair(v1, v2));
			}
			// only evaluate when we have at least 25% of the sample data
			if (subset.size() < data.size() * 0.75)
				if (evaluateSubset(workingList))
				{
					System.out.println("evaluated " + (data.size() - subset.size() + " / " + data.size()));
					return true;
				}
		}
		return false;

	}

	/**
	 * 
	 * @param workingList
	 * @return true if there is no need to process further combinations
	 */
	abstract boolean evaluateSubset(List<Pair<T>> workingList);

	abstract Pair<T> createPair(T v1, T v2);
}
