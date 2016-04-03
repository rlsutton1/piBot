package au.com.rsutton.mapping.probability;

import java.util.Arrays;

import au.com.rsutton.mapping.array.Dynamic2dSparseArray;

public class ProbabilityMap
{

	private int blockSize;
	final private Dynamic2dSparseArray world;

	public ProbabilityMap(int blockSize)
	{

		world = new Dynamic2dSparseArray(0.5);

		this.blockSize = blockSize;
	}

	public double[][] createGausian(int radius, double sigma)
	{

		int W = (radius * 2) + 1;
		double[][] kernel = new double[W][W];
		double mean = W / 2;
		double sum = 0.0; // For accumulating the kernel values
		for (int x = 0; x < W; ++x)
			for (int y = 0; y < W; ++y)
			{
				kernel[x][y] = Math.exp(-0.5 * (Math.pow((x - mean) / sigma, 2.0) + Math.pow((y - mean) / sigma, 2.0)))
						/ (2 * Math.PI * sigma * sigma);

				// Accumulate the kernel values
				sum += kernel[x][y];
			}

		// Normalize the kernel
		for (int x = 0; x < W; ++x)
			for (int y = 0; y < W; ++y)
				kernel[x][y] /= sum;

		return kernel;
	}

	public void updatePoint(int x, int y, double probability, int gausianRadius)
	{

		// scale gausianRadis by blockSize

		x = x / blockSize;
		y = y / blockSize;

		int gr = Math.min(1, gausianRadius / blockSize);
		double[][] occupancyProbability = createGausian(gr, probability);

		occupancyProbability = shiftProbability(occupancyProbability, 0.2, 0.3);
		// scale x and y by blocksize

		int xl = occupancyProbability.length;
		int yl = occupancyProbability[0].length;
		if (xl % 2 != 1 || yl % 2 != 1)
		{
			throw new RuntimeException("OccupancyProbability array must be an odd number of rows and colums");
		}

		int xstart = -xl / 2;
		int ystart = -yl / 2;

		for (int xc = 0; xc < xl; xc++)
		{
			for (int yc = 0; yc < yl; yc++)
			{
				int xpos = x + xc + xstart;
				int ypos = y + yc + ystart;

				updatePoint(xpos, ypos, occupancyProbability[xc][yc]);

			}

		}
	}

	/**
	 * 
	 * @param occupancyProbability
	 * @param xShiftAmount
	 *            between -0.5 and 0.5
	 * @param yShiftAmount
	 *            between -0.5 and 0.5
	 * @return
	 */
	private double[][] shiftProbability(double[][] occupancyProbability, double xShiftAmount, double yShiftAmount)
	{

		int shiftMatrixSize = 3;

		double[][] shift = createShiftMatrix(xShiftAmount, yShiftAmount);

		double[][] result = Arrays.copyOf(occupancyProbability, occupancyProbability.length);

		for (int x = 0; x < occupancyProbability[0].length; x++)
		{
			for (int y = 0; y < occupancyProbability.length; y++)
			{

				for (int xs = 0; xs < shiftMatrixSize; xs++)
				{
					for (int ys = 0; ys < shiftMatrixSize; ys++)
					{
						int xi = x + xs - 1;
						int yi = y + ys - 1;
						if (xi > 0 && yi > 0 && xi < result[0].length && yi < result.length)
							result[x][y] += result[xi][yi] * shift[xs][ys];
					}
				}
			}
		}
		return result;
	}

	public double[][] createShiftMatrix(double xShiftAmount, double yShiftAmount)
	{
		// System.out.println("xs: " + xShiftAmount + " ys: " + yShiftAmount);
		int shiftMatrixSize = 3;

		double[][] shift = new double[shiftMatrixSize][shiftMatrixSize];

		double xc = (1.0 - Math.abs(xShiftAmount));
		double yc = (1.0 - Math.abs(yShiftAmount));

		double xl = Math.max(0.0, 0.0 - xShiftAmount);
		double yt = Math.max(0.0, 0.0 - yShiftAmount);

		double xr = Math.max(0.0, 0.0 + xShiftAmount);
		double yb = Math.max(0.0, 0.0 + yShiftAmount);

		shift = new double[][] {
				{
						xl * yt, yt * xc, xr * yt }, {
						xl * yc, yc * xc, xr * yc }, {
						xl * yb, yb * xc, xr * yb } };

		double total = 0.0;
		for (int xs = 0; xs < shiftMatrixSize; xs++)
		{
			for (int ys = 0; ys < shiftMatrixSize; ys++)
			{
				// System.out.print(shift[xs][ys] + " ");
				total += shift[xs][ys];
			}
			// System.out.println("");
		}
		if (Math.abs(1.0 - total) > 0.01)
		{
			// System.out.println("Error total probability is " + total);
		}
		return shift;
	}

	private void updatePoint(int x, int y, double occupancyProbability)
	{

		world.set(x, y, world.get(x, y) + ((0.9995 - world.get(x, y)) * occupancyProbability));
	}

	public void dumpWorld()
	{

		for (int x = world.getMinX() - 1; x < world.getMaxX() + 1; x++)
		{
			for (int y = world.getMinY() - 1; y < world.getMaxY() + 1; y++)
			{
				String v = String.format("%.4f, ", world.get(x, y));
				System.out.print(v);
			}
			System.out.println();
		}
	}

	public void dumpTextWorld()
	{

		Boolean[] line = new Boolean[Math.abs(world.getMinX() - world.getMaxX()) + 60];
		for (int i = 0; i < line.length; i++)
		{
			line[i] = false;
		}
		System.out.println(world.getMinX() * blockSize + "," + world.getMinY() * blockSize);
		for (int y = world.getMinY() - 1; y < world.getMaxY() + 1; y++)
		{
			for (int x = world.getMinX() - 1; x < world.getMaxX() + 1; x++)
			{
				if (world.get(x, y) >= 0.7)
				{
					line[x + Math.abs(world.getMinX()) + 1] |= true;
					// System.out.print("*");
				} else
				{
					// System.out.print(".");
				}
			}
			if (y % 2 == 0)
			{
				for (int x = world.getMinX() - 1; x < world.getMaxX() + 1; x++)
				{
					if (line[x + Math.abs(world.getMinX()) + 1])
					{
						System.out.print("*");

					} else
					{
						System.out.print(".");
					}
				}
				System.out.println();
				for (int i = 0; i < line.length; i++)
				{
					line[i] = false;
				}
			} else
			{

			}

		}
		System.out.println(world.getMaxX() * blockSize + "," + world.getMaxY() * blockSize);
	}

	public int getMaxX()
	{
		return world.getMaxX() * blockSize;
	}

	public int getMinX()
	{
		return world.getMinX() * blockSize;
	}

	public int getMaxY()
	{
		return world.getMaxY() * blockSize;
	}

	public int getMinY()
	{
		return world.getMinY() * blockSize;
	}

	public double get(double x, double y)
	{
		return world.get((int) x / blockSize, (int) y / blockSize);
	}

	public int getBlockSize()
	{
		return blockSize;
	}
}
