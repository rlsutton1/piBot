package au.com.rsutton.navigation.router.md;

import org.junit.Test;

import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.maps.KitchenMapBuilder;

public class RoutePlanner3DTest
{

	@Test
	public void test()
	{

		int[][] map = buildMap();

		RoutePlanner3D planner = new RoutePlanner3D(map, 72);

		MoveTemplate straight = new MoveTemplate(1, new RPAngle(0), "F", true);
		MoveTemplate softRight = new MoveTemplate(5, new RPAngle(5), "l", true);
		MoveTemplate softLeft = new MoveTemplate(5, new RPAngle(-5), "r", true);
		MoveTemplate hardRight = new MoveTemplate(20, new RPAngle(10), "L", true);
		MoveTemplate hardLeft = new MoveTemplate(20, new RPAngle(-10), "R", true);
		MoveTemplate reverse = new MoveTemplate(100, new RPAngle(0), "B", true);

		MoveTemplate[] moveTemplates = new MoveTemplate[] {
				straight, softRight, softLeft, hardRight, hardLeft, //
				reverse,

		};

		System.out.println("Start plan");
		planner.plan(new RpPose(60, 10, new RPAngle(90)), moveTemplates);

		System.out.println("Start dump");

		moveTemplates = new MoveTemplate[] {
				straight, softRight, softLeft, // hardRight, hardLeft //
												// reverseLeft,
				// reverseRight,

		};
		for (int i = 0; i < 360; i += 45)
		{
			planner.dumpFrom(new RpPose(30, 125, new RPAngle(i)));
		}

		// planner.dumpMap();
	}

	private int[][] buildMap()
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

		double[][] costKernal = new double[][] {
				{
						0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01 },
				{
						0.01, 0.02, 0.02, 0.02, 0.02, 0.02, 0.01 },
				{
						0.01, 0.02, 0.03, 0.03, 0.03, 0.02, 0.01 },
				{
						0.01, 0.02, 0.03, 1.0, 0.03, 0.02, 0.01 },
				{
						0.01, 0.02, 0.03, 0.03, 0.03, 0.02, 0.01 },
				{
						0.01, 0.02, 0.02, 0.02, 0.02, 0.02, 0.01 },
				{
						0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01 }

		};

		double[][] configurationSpaceKernal = new double[][] {
				{
						1, 1, 1, 1, 1 },
				{
						1, 1, 1, 1, 1 },
				{
						1, 1, 1, 1, 1 },
				{
						1, 1, 1, 1, 1 },
				{
						1, 1, 1, 1, 1 }, };

		int[][] temp = RoutePlannerAdapter.applyKernal(map, configurationSpaceKernal);
		return RoutePlannerAdapter.applyKernal(temp, costKernal);
	}

}
