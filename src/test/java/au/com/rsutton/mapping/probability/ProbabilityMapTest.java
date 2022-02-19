package au.com.rsutton.mapping.probability;

import java.util.Random;

import org.junit.Test;

public class ProbabilityMapTest
{

	@Test
	public void test()
	{
		int worldSize = 50;
		ProbabilityMapIIFc world = new ProbabilityMap(1, 0.5);

		Random r = new Random();

		for (int z = 0; z < 20; z++)
			for (int i = 0; i < worldSize; i++)
			{

				int noise = r.nextInt(8) - 4;

				int x = i + noise;
				if (x > 0 && x < worldSize)
				{
					world.updatePoint(x, x, Occupancy.OCCUPIED, 1.0, 2);
				}
			}

		world.dumpWorld();

		world.dumpTextWorld();

	}

	@Test
	public void testUp()
	{
		ProbabilityMapIIFc world = new ProbabilityMap(1, 0.5);

		for (int i = 0; i < 20; i++)
		{
			world.updatePoint(1, 1, Occupancy.OCCUPIED, 1.0, 2);
			System.out.println("Prob: " + world.get(1, 1));
		}
	}

	@Test
	public void drawWorld()
	{
		ProbabilityMapIIFc world = new ProbabilityMap(10, 0.5);
		world.drawLine(0, 0, 0, 89, Occupancy.OCCUPIED, 1.0, 2);// a
		world.drawLine(0, 0, -141, 0, Occupancy.OCCUPIED, 1.0, 2);// b
		world.drawLine(-141, 0, -141, 42, Occupancy.OCCUPIED, 1.0, 2);// c
		world.drawLine(-141, 42, -266, 42, Occupancy.OCCUPIED, 1.0, 2);// d

		world.drawLine(-266, 42, -266, 180, Occupancy.OCCUPIED, 1.0, 2);//
		world.drawLine(-266, 180, -194, 180, Occupancy.OCCUPIED, 1.0, 2);// e
		world.drawLine(-194, 180, -194, 252, Occupancy.OCCUPIED, 1.0, 2);// f
		world.drawLine(-194, 252, -223, 252, Occupancy.OCCUPIED, 1.0, 2);// g
		world.drawLine(-223, 252, -223, 407, Occupancy.OCCUPIED, 1.0, 2);// h
		world.drawLine(-223, 407, -49, 407, Occupancy.OCCUPIED, 1.0, 2);// i
		world.drawLine(-49, 407, -49, 325, Occupancy.OCCUPIED, 1.0, 2);// j
		world.drawLine(-49, 325, -57, 325, Occupancy.OCCUPIED, 1.0, 2);// k
		world.drawLine(-57, 325, -57, 170, Occupancy.OCCUPIED, 1.0, 2);// l
		world.drawLine(-57, 170, 0, 170, Occupancy.OCCUPIED, 1.0, 2);// m

		world.drawLine(-57, 170, 110, 170, Occupancy.OCCUPIED, 1.0, 2);// m
		world.drawLine(110, 170, 110, -86, Occupancy.OCCUPIED, 1.0, 2);// m

		world.drawLine(0, 0, 0, -346, Occupancy.OCCUPIED, 1.0, 2);// m

		world.drawLine(110, -86, 165, -86, Occupancy.OCCUPIED, 1.0, 2);// m
		world.drawLine(165, -86, 165, -346, Occupancy.OCCUPIED, 1.0, 2);// m
		world.drawLine(165, -346, 0, -346, Occupancy.OCCUPIED, 1.0, 2);// m

		world.dumpTextWorld();

	}

}
