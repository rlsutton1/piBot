package au.com.rsutton.mapping.probability;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.base.Preconditions;

import au.com.rsutton.mapping.array.Dynamic2dSparseArray;
import au.com.rsutton.mapping.array.SparseArray;
import au.com.rsutton.robot.RobotSimulator;
import au.com.rsutton.ui.DataSourcePoint;

public class ProbabilityMap implements DataSourcePoint, ProbabilityMapIIFc
{

	private int blockSize;
	private SparseArray<Double> world;
	private double defaultValue = 0.5;

	public ProbabilityMap(int blockSize, double defaultValue)
	{

		world = new Dynamic2dSparseArray<>(defaultValue);
		this.defaultValue = defaultValue;

		this.blockSize = blockSize;
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
		double max = 0.0;
		for (int x = 0; x < W; ++x)
			for (int y = 0; y < W; ++y)
			{
				kernel[x][y] = Math.exp(-0.5 * (Math.pow((x - mean) / sigma, 2.0) + Math.pow((y - mean) / sigma, 2.0)))
						/ (2 * Math.PI * sigma * sigma);

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

	@Override
	public double getSubPixelValue(double x, double y)
	{
		double xa = x / blockSize;
		double ya = y / blockSize;
		int x1 = (int) xa;
		int y1 = (int) ya;

		int x2 = x1 + 1;
		int y2 = y1 + 1;

		double s1 = 1 - Math.max(Math.abs(x1 - xa), Math.abs(y1 - ya));
		double s2 = 1 - Math.max(Math.abs(x2 - xa), Math.abs(y1 - ya));
		double s3 = 1 - Math.max(Math.abs(x2 - xa), Math.abs(y2 - ya));
		double s4 = 1 - Math.max(Math.abs(x1 - xa), Math.abs(y2 - ya));

		double t = s1 + s2 + s3 + s4;

		s1 = s1 / t;
		s2 = s2 / t;
		s3 = s3 / t;
		s4 = s4 / t;

		double r = s1 * world.get(x1, y1);
		r += s2 * world.get(x2, y1);
		r += s3 * world.get(x2, y2);
		r += s4 * world.get(x1, y2);

		return r;
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
		world = new Dynamic2dSparseArray<>(defaultValue);

	}

	public void writeRadius(int x, int y, double value, int radius)
	{
		for (int ox = -radius; ox < radius; ox += blockSize)
			for (int oy = -radius; oy < radius; oy += blockSize)
				world.set((x + ox) / blockSize, (y + oy) / blockSize, value);
	}

	@Override
	public void convertToDenseOffsetArray()
	{
		world = ((Dynamic2dSparseArray<Double>) world).copyAsDenseOffsetArray();
	}

	@Override
	public void save(File file)
	{
		int sizeX = world.getMaxX() - world.getMinX();
		sizeX += 4;

		int sizeY = world.getMaxY() - world.getMinY();
		sizeY += 4;

		final BufferedImage res = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_RGB);
		for (int x = world.getMinX() - 1; x < world.getMaxX() + 1; x += 1)
		{
			for (int y = world.getMinY() - 1; y < world.getMaxY() + 1; y += 1)
			{
				if (world.get(x, y) >= RobotSimulator.REQUIRED_POINT_CERTAINTY)
				{
					res.setRGB(2 + x - world.getMinX(), 2 + y - world.getMinY(), new Color(255, 255, 255).getRGB());
				} else if (world.get(x, y) < 0.5)
				{
					// write un-occupied
					res.setRGB(2 + x - world.getMinX(), 2 + y - world.getMinY(), 0);
				} else
				{
					// write unexplored
					res.setRGB(2 + x - world.getMinX(), 2 + y - world.getMinY(), new Color(64, 64, 64).getRGB());
				}
			}

		}

		try
		{
			RenderedImage rendImage = res;
			ImageIO.write(rendImage, "bmp", file);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	void load(String filename)
	{

	}

	@Override
	public void setValue(double x, double y, double value)
	{
		world.set((int) x / blockSize, (int) y / blockSize, value);

	}

}
