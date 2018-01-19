package au.com.rsutton.mapping;

import au.com.rsutton.mapping.probability.Occupancy;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;

public class KitchenMapBuilder
{

	public static ProbabilityMap buildKitchenMap()
	{
		ProbabilityMap world = new ProbabilityMap(5);

		buildKitchenMap(world, 2);

		return world;

	}

	public static ProbabilityMap buildKitchenMapMatcher()
	{

		ProbabilityMap pfmatch = new ProbabilityMap(5);

		buildKitchenMap(pfmatch, 25);

		return pfmatch;

	}

	public static void buildKitchenMap(ProbabilityMapIIFc world, int radius)
	{
		// table leg
		drawLine(world, -120, 60, -120, 70, Occupancy.OCCUPIED, 1.0, radius);

		// bin
		// drawLine(world,-40,70,-10,70, Occupancy.OCCUPIED, 1.0, radius);
		// drawLine(world,-40,70,-40,40, Occupancy.OCCUPIED, 1.0, radius);
		// drawLine(world,-10,40,-10,70, Occupancy.OCCUPIED, 1.0, radius);
		// drawLine(world,-40,40,-10,40, Occupancy.OCCUPIED, 1.0, radius);

		drawLine(world, 0, 0, 0, 89, Occupancy.OCCUPIED, 1.0, radius);// a
		drawLine(world, 0, 0, -141, 0, Occupancy.OCCUPIED, 1.0, radius);// b
		drawLine(world, -141, 0, -141, 42, Occupancy.OCCUPIED, 1.0, radius);// c
		drawLine(world, -141, 42, -266, 42, Occupancy.OCCUPIED, 1.0, radius);// d

		drawLine(world, -266, 42, -266, 180, Occupancy.OCCUPIED, 1.0, radius);//
		drawLine(world, -266, 180, -194, 180, Occupancy.OCCUPIED, 1.0, radius);// e
		drawLine(world, -194, 180, -194, 252, Occupancy.OCCUPIED, 1.0, radius);// f
		drawLine(world, -194, 252, -223, 252, Occupancy.OCCUPIED, 1.0, radius);// g
		drawLine(world, -223, 252, -223, 407, Occupancy.OCCUPIED, 1.0, radius);// h
		drawLine(world, -223, 407, -49, 407, Occupancy.OCCUPIED, 1.0, radius);// i
		drawLine(world, -49, 407, -49, 325, Occupancy.OCCUPIED, 1.0, radius);// j
		drawLine(world, -49, 325, -57, 325, Occupancy.OCCUPIED, 1.0, radius);// k
		drawLine(world, -57, 325, -57, 170, Occupancy.OCCUPIED, 1.0, radius);// l
		drawLine(world, -57, 170, 0, 170, Occupancy.OCCUPIED, 1.0, radius);// m

		drawLine(world, -57, 170, 110, 170, Occupancy.OCCUPIED, 1.0, radius);// m
		drawLine(world, 110, 170, 110, -86, Occupancy.OCCUPIED, 1.0, radius);// m

		drawLine(world, 0, 0, 0, -346, Occupancy.OCCUPIED, 1.0, radius);// m

		drawLine(world, 110, -86, 165, -86, Occupancy.OCCUPIED, 1.0, radius);// m
		drawLine(world, 165, -86, 165, -346, Occupancy.OCCUPIED, 1.0, radius);// m
		drawLine(world, 165, -346, 0, -346, Occupancy.OCCUPIED, 1.0, radius);// m

		world.dumpTextWorld();
	}

	private static void drawLine(ProbabilityMapIIFc world, int x1, int y1, int x2, int y2, Occupancy occupied,
			double certainty, int radius)
	{
		world.drawLine(x1, y1, x2, y2, occupied, certainty, radius);

	}

}
