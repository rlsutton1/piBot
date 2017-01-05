package au.com.rsutton.mapping;

import au.com.rsutton.mapping.probability.Occupancy;
import au.com.rsutton.mapping.probability.ProbabilityMap;

public class KitchenMapBuilder
{

	public static ProbabilityMap buildKitchenMap()
	{
		ProbabilityMap world = new ProbabilityMap(5);

		buildKitchenMap(world);

		return world;

	}

	public static void buildKitchenMap(ProbabilityMap world)
	{
		// table leg
		world.drawLine(-120, 60, -120, 70, Occupancy.OCCUPIED, 1.0, 2);

		// bin
		// world.drawLine(-40,70,-10,70, Occupancy.OCCUPIED, 1.0, 2);
		// world.drawLine(-40,70,-40,40, Occupancy.OCCUPIED, 1.0, 2);
		// world.drawLine(-10,40,-10,70, Occupancy.OCCUPIED, 1.0, 2);
		// world.drawLine(-40,40,-10,40, Occupancy.OCCUPIED, 1.0, 2);

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
