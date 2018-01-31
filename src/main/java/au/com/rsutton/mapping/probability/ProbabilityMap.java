package au.com.rsutton.mapping.probability;

import java.awt.Point;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.base.Preconditions;

import au.com.rsutton.mapping.array.Dynamic2dSparseArray;
import au.com.rsutton.mapping.array.SparseArray;
import au.com.rsutton.robot.RobotSimulator;
import au.com.rsutton.ui.DataSourcePoint;

public class ProbabilityMap implements DataSourcePoint, ProbabilityMapIIFc
{

	private int blockSize;
	private SparseArray world;
	private double defaultValue = 0.5;

	public ProbabilityMap(int blockSize)
	{

		world = new Dynamic2dSparseArray(defaultValue);

		this.blockSize = blockSize;
	}

	public void setDefaultValue(double defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * au.com.rsutton.mapping.probability.ProbabilityMapIIFc#createGausian(int,
	 * double, double)
	 */
	@Override
	public double[][] createGausian(int radius, double sigma, double centerValue)
	{

		int W = (radius * 2) + 1;
		double[][] kernel = new double[W][W];
		double mean = W / 2;
		double sum = 0.0; // For accumulating the kernel values
		double max = 0.0;
		for (int x = 0; x < W; ++x)
			for (int y = 0; y < W; ++y)
			{
				kernel[x][y] = Math.exp(-0.5 * (Math.pow((x - mean) / sigma, 2.0) + Math.pow((y - mean) / sigma, 2.0)))
						/ (2 * Math.PI * sigma * sigma);

				// Accumulate the kernel values
				sum += kernel[x][y];
				max = Math.max(max, kernel[x][y]);
			}

		// create a scaler
		double scaler = centerValue / max;

		// Normalize the kernel, with a center value o centerValue
		for (int x = 0; x < W; ++x)
			for (int y = 0; y < W; ++y)
			{
				kernel[x][y] *= scaler;
				// kernel[x][y] /= sum;
			}

		return kernel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * au.com.rsutton.mapping.probability.ProbabilityMapIIFc#resetPoint(int,
	 * int)
	 */
	@Override
	public void resetPoint(int x, int y)
	{
		x = x / blockSize;
		y = y / blockSize;
		world.set(x, y, world.getDefaultValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * au.com.rsutton.mapping.probability.ProbabilityMapIIFc#updatePoint(int,
	 * int, au.com.rsutton.mapping.probability.Occupancy, double, int)
	 */
	@Override
	public void updatePoint(int x, int y, Occupancy occupied, double certainty, int gausianRadius)
	{

		Preconditions.checkArgument(certainty >= 0 && certainty <= 1.0, "Certainty must be between 0.0 and 1.0");

		// scale gausianRadis by blockSize

		x = x / blockSize;
		y = y / blockSize;

		int gr = Math.min(1, gausianRadius / blockSize);
		double[][] occupancyProbability = createGausian(gr, 1.0, 1.0);

		// occupancyProbability = shiftProbability(occupancyProbability, 0.2,
		// 0.3);
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

				double gausian = occupancyProbability[xc][yc];

				double centered = certainty * gausian;

				updatePoint(xpos, ypos, occupied, centered);

			}

		}
	}

	List<Vector3D> features = new LinkedList<>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see au.com.rsutton.mapping.probability.ProbabilityMapIIFc#getFeatures()
	 */
	@Override
	public List<Vector3D> getFeatures()
	{
		return features;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * au.com.rsutton.mapping.probability.ProbabilityMapIIFc#drawLine(double,
	 * double, double, double, au.com.rsutton.mapping.probability.Occupancy,
	 * double, int)
	 */
	@Override
	public void drawLine(double x1, double y1, double x2, double y2, Occupancy occupancy, double certainty, int radius)
	{

		// consider every vertex is a feature
		Vector3D lineStart = new Vector3D(x2, y2, 0);
		Vector3D lineEnd = new Vector3D(x1, y1, 0);
		features.add(lineEnd);
		features.add(lineStart);

		double length = Vector3D.distance(lineStart, lineEnd);

		for (double i = 0; i < length; i++)
		{
			double percent = i / length;
			double x = (x1 * percent) + (x2 * (1.0 - percent));
			double y = (y1 * percent) + (y2 * (1.0 - percent));

			updatePoint((int) x, (int) y, occupancy, certainty, radius);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * au.com.rsutton.mapping.probability.ProbabilityMapIIFc#createShiftMatrix(
	 * double, double)
	 */
	private double[][] createShiftMatrix(double xShiftAmount, double yShiftAmount)
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
						xl * yt, yt * xc, xr * yt },
				{
						xl * yc, yc * xc, xr * yc },
				{
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

	private void updatePoint(int x, int y, Occupancy occupied, double certainty)
	{
		Preconditions.checkArgument(certainty >= 0 && certainty <= 1.0, "Certainty must be between 0.0 and 1.0");

		double currentValue = world.get(x, y);
		if (occupied == Occupancy.OCCUPIED)
		{
			double delta = certainty * (1.0 - currentValue);
			world.set(x, y, currentValue + delta);
		} else
		{
			double delta = certainty * currentValue;
			world.set(x, y, currentValue - delta);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see au.com.rsutton.mapping.probability.ProbabilityMapIIFc#dumpWorld()
	 */
	@Override
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * au.com.rsutton.mapping.probability.ProbabilityMapIIFc#dumpTextWorld()
	 */
	@Override
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
				if (world.get(x, y) >= 0.51)
				{
					line[(x - world.getMinX()) + 1] |= true;
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
					if (line[(x - world.getMinX()) + 1])
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see au.com.rsutton.mapping.probability.ProbabilityMapIIFc#getMaxX()
	 */
	@Override
	public int getMaxX()
	{
		return world.getMaxX() * blockSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see au.com.rsutton.mapping.probability.ProbabilityMapIIFc#getMinX()
	 */
	@Override
	public int getMinX()
	{
		return world.getMinX() * blockSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see au.com.rsutton.mapping.probability.ProbabilityMapIIFc#getMaxY()
	 */
	@Override
	public int getMaxY()
	{
		return world.getMaxY() * blockSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see au.com.rsutton.mapping.probability.ProbabilityMapIIFc#getMinY()
	 */
	@Override
	public int getMinY()
	{
		return world.getMinY() * blockSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see au.com.rsutton.mapping.probability.ProbabilityMapIIFc#get(double,
	 * double)
	 */
	@Override
	public double get(double x, double y)
	{
		return world.get((int) x / blockSize, (int) y / blockSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see au.com.rsutton.mapping.probability.ProbabilityMapIIFc#getBlockSize()
	 */
	@Override
	public int getBlockSize()
	{
		return blockSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * au.com.rsutton.mapping.probability.ProbabilityMapIIFc#getOccupiedPoints()
	 */
	@Override
	public List<Point> getOccupiedPoints()
	{
		List<Point> points = new LinkedList<>();
		for (int x = world.getMinX() - 1; x < world.getMaxX() + 1; x += 1)
		{
			for (int y = world.getMinY() - 1; y < world.getMaxY() + 1; y += 1)
			{
				if (world.get(x, y) >= RobotSimulator.REQUIRED_POINT_CERTAINTY)
				{
					points.add(new Point(x * blockSize, y * blockSize));
				}
			}

		}
		return points;
	}

	@Override
	public void erase()
	{
		world = new Dynamic2dSparseArray(defaultValue);

	}
}
