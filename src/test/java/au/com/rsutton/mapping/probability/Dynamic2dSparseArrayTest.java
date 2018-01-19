package au.com.rsutton.mapping.probability;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.google.common.base.Stopwatch;

import au.com.rsutton.mapping.array.Dynamic2dSparseArrayFactory;
import au.com.rsutton.mapping.array.SparseArray;

public class Dynamic2dSparseArrayTest
{

	@Test
	public void test()
	{

		int min = -999999;
		int max = 999999;

		Stopwatch timer = Stopwatch.createStarted();
		SparseArray map = Dynamic2dSparseArrayFactory.getDynamic2dSparseArray(0.5);
		for (int i = min; i < max; i++)
		{
			map.set(i, i, i);
			// System.out.println(i);
		}

		for (int i = min; i < max; i++)
		{
			if (i != map.get(i, i))
			{
				System.out.println("Error " + i);
			}
		}

		System.out.println("Elapsed " + timer.elapsed(TimeUnit.MILLISECONDS));
	}

}
