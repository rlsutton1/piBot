package au.com.rsutton.mapping.probability;

import org.junit.Test;

import au.com.rsutton.mapping.array.Dynamic2dSparseArray;

public class Dynamic2dSparseArrayTest
{

	@Test
	public void test()
	{
		Dynamic2dSparseArray map = new Dynamic2dSparseArray(0.5);
		for (int i = -9999; i < 10000; i++)
		{
			map.set(i, i, i);
			System.out.println(i);
		}

		for (int i = -9999; i < 10000; i++)
		{
			if (i != map.get(i, i))
			{
				System.out.println("Error " + i);
			}
		}
	}

}
