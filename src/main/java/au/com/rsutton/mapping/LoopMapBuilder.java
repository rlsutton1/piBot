package au.com.rsutton.mapping;

import au.com.rsutton.mapping.probability.Occupancy;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;

public class LoopMapBuilder
{

	private static final int MULTIPLIER = 50;

	public static ProbabilityMap buildMap()
	{
		ProbabilityMap world = new ProbabilityMap(5);

		buildKitchenMap(world, 2);

		return world;

	}

	public static void buildKitchenMap(ProbabilityMapIIFc world, int radius)
	{
		// table leg
		drawLine(world, 0 * MULTIPLIER, 0 * MULTIPLIER, 0 * MULTIPLIER, 8 * MULTIPLIER, Occupancy.OCCUPIED, 1.0,
				radius);
		drawLine(world, 0 * MULTIPLIER, 0 * MULTIPLIER, 8 * MULTIPLIER, 0 * MULTIPLIER, Occupancy.OCCUPIED, 1.0,
				radius);
		drawLine(world, 8 * MULTIPLIER, 0 * MULTIPLIER, 8 * MULTIPLIER, 8 * MULTIPLIER, Occupancy.OCCUPIED, 1.0,
				radius);
		drawLine(world, 0 * MULTIPLIER, 8 * MULTIPLIER, 8 * MULTIPLIER, 8 * MULTIPLIER, Occupancy.OCCUPIED, 1.0,
				radius);

		drawLine(world, 2 * MULTIPLIER, 2 * MULTIPLIER, 2 * MULTIPLIER, 6 * MULTIPLIER, Occupancy.OCCUPIED, 1.0,
				radius);
		drawLine(world, 2 * MULTIPLIER, 2 * MULTIPLIER, 6 * MULTIPLIER, 2 * MULTIPLIER, Occupancy.OCCUPIED, 1.0,
				radius);
		drawLine(world, 6 * MULTIPLIER, 2 * MULTIPLIER, 6 * MULTIPLIER, 6 * MULTIPLIER, Occupancy.OCCUPIED, 1.0,
				radius);
		drawLine(world, 2 * MULTIPLIER, 6 * MULTIPLIER, 6 * MULTIPLIER, 6 * MULTIPLIER, Occupancy.OCCUPIED, 1.0,
				radius);

		// drawLine(world, 4 * MULTIPLIER, 0 * MULTIPLIER, 4 * MULTIPLIER, 2 *
		// MULTIPLIER, Occupancy.OCCUPIED, 1.0,
		// radius);

		world.dumpTextWorld();
	}

	private static void drawLine(ProbabilityMapIIFc world, int x1, int y1, int x2, int y2, Occupancy occupied,
			double certainty, int radius)
	{
		world.drawLine(x1, y1, x2, y2, occupied, certainty, radius);

	}

}
