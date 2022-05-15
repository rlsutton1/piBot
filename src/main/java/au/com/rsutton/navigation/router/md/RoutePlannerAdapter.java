package au.com.rsutton.navigation.router.md;

import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.navigation.router.RoutePlanner;

public class RoutePlannerAdapter implements RoutePlanner
{

	private RoutePlanner3D planner;
	private MoveTemplate[] moveTemplates;
	private boolean planned = false;
	private int xOffset;
	private int yOffset;
	private int blockSize;

	public RoutePlannerAdapter()
	{
		MoveTemplate straight = new MoveTemplate(1, new RPAngle(0), "F", true);
		MoveTemplate softRight = new MoveTemplate(5, new RPAngle(5), "l", true);
		MoveTemplate softLeft = new MoveTemplate(5, new RPAngle(-5), "r", true);
		MoveTemplate hardRight = new MoveTemplate(20, new RPAngle(10), "L", true);
		MoveTemplate hardLeft = new MoveTemplate(20, new RPAngle(-10), "R", true);
		MoveTemplate reverse = new MoveTemplate(100, new RPAngle(0), "B", true);

		moveTemplates = new MoveTemplate[] {
				straight, softRight, softLeft, hardRight, hardLeft, //
				reverse,

		};
	}

	@Override
	public void createPlannerForMap(ProbabilityMapIIFc probabilityMap)
	{
		int[][] map = convertProbabilityMapToArray(probabilityMap);

		map = applyKernal(map, createConfigurationSpaceKernal());
		map = applyKernal(map, createCostKernal());

		planned = false;
		planner = new RoutePlanner3D(map, 72);
		xOffset = probabilityMap.getMinX();
		yOffset = probabilityMap.getMinY();
		blockSize = probabilityMap.getBlockSize();

	}

	@Override
	public void plan(int x, int y, double heading)
	{
		planner.plan(new RpPose(x, y, new RPAngle((int) heading)), moveTemplates);
		planned = true;
	}

	@Override
	public MoveTemplate getNextMove(int x, int y, double heading)
	{
		int tx = (x - xOffset) / blockSize;
		int ty = (y - yOffset) / blockSize;
		return planner.getNextMove(new RpPose(tx, ty, new RPAngle((int) heading)));
	}

	private int[][] convertProbabilityMapToArray(ProbabilityMapIIFc map1)
	{
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
		return map;
	}

	private double[][] createCostKernal()
	{
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
		return costKernal;
	}

	private double[][] createConfigurationSpaceKernal()
	{
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
		return configurationSpaceKernal;
	}

	static public int[][] applyKernal(int[][] source, double[][] kernal)
	{
		int[][] output = new int[source.length][source[0].length];

		for (int x = 0; x < source.length; x++)
		{
			for (int y = 0; y < source[0].length; y++)
			{
				output[x][y] = calculateKernalValue(x, y, source, kernal);
			}
		}

		return output;

	}

	static private int calculateKernalValue(int x, int y, int[][] source, double[][] kernal)
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

	@Override
	public boolean hasPlannedRoute()
	{
		return planned;
	}
}
