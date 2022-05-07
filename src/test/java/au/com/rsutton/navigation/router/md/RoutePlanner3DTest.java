package au.com.rsutton.navigation.router.md;

import org.junit.Test;

import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.maps.KitchenMapBuilder;
import au.com.rsutton.navigation.router.md.RoutePlanner3D.MoveTemplate;

public class RoutePlanner3DTest
{

	@Test
	public void test()
	{

		ProbabilityMap map1 = KitchenMapBuilder.buildMap();
		int blockSize = map1.getBlockSize();
		int minX = map1.getMinX() / blockSize;
		int minY = map1.getMinY() / blockSize;
		int maxX = map1.getMaxX() / blockSize;
		int maxY = map1.getMaxY() / blockSize;
		int xr = maxX - minX;
		int yr = maxY - minY;
		int[][] map = new int[xr][yr];
		for (int x = 0; x < xr; x++)
		{
			for (int y = 0; y < yr; y++)
			{
				double value = map1.get((minX + x) * blockSize, (minY + y) * blockSize);
				if (value > 0.5)
				{
					map[x][y] = Integer.MAX_VALUE;
				} else
				{
					map[x][y] = 0;
				}
			}
		}

		RoutePlanner3D planner = new RoutePlanner3D(map, 72);

		MoveTemplate straight = planner.moveTemplateFactory(1, planner.angleFactory(0));
		MoveTemplate softRight = planner.moveTemplateFactory(5, planner.angleFactory(5));
		MoveTemplate softLeft = planner.moveTemplateFactory(5, planner.angleFactory(-5));
		MoveTemplate hardRight = planner.moveTemplateFactory(20, planner.angleFactory(10));
		MoveTemplate hardLeft = planner.moveTemplateFactory(20, planner.angleFactory(-10));
		MoveTemplate reverseLeft = planner.moveTemplateFactory(200, planner.angleFactory(170));
		MoveTemplate reverseRight = planner.moveTemplateFactory(200, planner.angleFactory(190));

		MoveTemplate[] moveTemplates = new MoveTemplate[] {
				straight, softRight, softLeft, hardRight, hardLeft, //
				reverseLeft, reverseRight,

		};

		System.out.println("Start plan");
		planner.plan(60, 10, planner.angleFactory(90), moveTemplates);

		System.out.println("Start dump");

		moveTemplates = new MoveTemplate[] {
				straight, softRight, softLeft, // hardRight, hardLeft //
												// reverseLeft,
				// reverseRight,

		};
		for (int i = 0; i < 360; i += 10)
		{
			planner.dumpFrom(30, 110, planner.angleFactory(i));
		}

		// planner.dumpMap();
	}

}
