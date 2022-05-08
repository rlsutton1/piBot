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

		int[][] map = buildMap();

		RoutePlanner3D planner = new RoutePlanner3D(map, 72);

		MoveTemplate straight = planner.moveTemplateFactory(1, planner.angleFactory(0), "F");
		MoveTemplate softRight = planner.moveTemplateFactory(5, planner.angleFactory(5), "l");
		MoveTemplate softLeft = planner.moveTemplateFactory(5, planner.angleFactory(-5), "r");
		MoveTemplate hardRight = planner.moveTemplateFactory(20, planner.angleFactory(10), "L");
		MoveTemplate hardLeft = planner.moveTemplateFactory(20, planner.angleFactory(-10), "R");
		MoveTemplate reverse = planner.moveTemplateFactory(200, planner.angleFactory(180), "B");

		MoveTemplate[] moveTemplates = new MoveTemplate[] {
				straight, softRight, softLeft, hardRight, hardLeft, //
				reverse,

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
			planner.dumpFrom(30, 125, planner.angleFactory(i));
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

		double[][] kernal = new double[][] {
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

		return applyKernalToMatrix(map, kernal);
	}

	int[][] applyKernalToMatrix(int[][] source, double[][] kernal)
	{
		int[][] output = new int[source.length][source[0].length];

		for (int x = 0; x < source.length; x++)
		{
			for (int y = 0; y < source[0].length; y++)
			{
				output[x][y] = applyKernal(x, y, source, kernal);
			}
		}

		return output;

	}

	private int applyKernal(int x, int y, int[][] source, double[][] kernal)
	{
		long result = 0;

		int sourceX = source.length;
		int sourceY = source[0].length;

		int kernalX = kernal.length;
		int kernalY = kernal[0].length;

		int halfKernalX = kernalX / 2;
		int halfKernalY = kernalY / 2;

		for (int kernalIdxX = 0; kernalIdxX < kernalX; kernalIdxX++)
		{

			for (int kernalIdxY = 0; kernalIdxY < kernalY; kernalIdxY++)
			{
				int sourceIdxX = (x + kernalIdxX) - halfKernalX;
				int sourceIdxY = (y + kernalIdxY) - halfKernalY;
				if (sourceIdxX >= 0 && sourceIdxY >= 0 && sourceIdxX < sourceX && sourceIdxY < sourceY)
				{
					result += source[sourceIdxX][sourceIdxY] * kernal[kernalIdxX][kernalIdxY];
				}
			}
		}

		if (result > Integer.MAX_VALUE)
		{
			result = Integer.MAX_VALUE;
		}

		return (int) result;
	}

}
