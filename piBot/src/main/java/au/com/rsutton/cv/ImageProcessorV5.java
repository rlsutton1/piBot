package au.com.rsutton.cv;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

import au.com.rsutton.hazelcast.ImageMessage;
import au.com.rsutton.mapping.CoordResolver;

public class ImageProcessorV5
{

	private static final double TOLLERANCE = 0.1;
	private static final double GREEN_RATIO = 0.1;
	private static final double RED_RATIO = 0.8;
	private static final int LINE_HEIGHT_TOLLERANCE = 5;
	double totalrb = 0;
	double totalrg = 0;
	double totalbg = 0;
	double count = 0;
	final CoordResolver coordResolver;

	public ImageProcessorV5(CoordResolver coordResolver)
	{
		this.coordResolver = coordResolver;
	}

	public Map<Integer, Integer> processImage(final BufferedImage src)
	{

		totalrb = 0;
		totalrg = 0;
		totalbg = 0;
		count = 0;

		Map<Integer, Integer> tmp = processImageInternal(src, 1.3d);

		if (count > 0)
		{
			System.out.println("average " + totalrb / count + " " + totalrg
					/ count + " " + totalbg / count);
		}
		return tmp;
	}

	public Map<Integer, Integer> processImageInternal(final BufferedImage src,
			double threshold)
	{
		Map<Integer, Integer> xy = new LinkedHashMap<>();

		long start = System.currentTimeMillis();
		BufferedImage bufferedImage = src;

		FastRGB fastRGB = new FastRGB(src);

		int width = bufferedImage.getWidth();

		for (int x = 5; x < width - 10; x += width / 100)
		{
			Block y = scanForPoint(fastRGB, x, bufferedImage.getHeight());
			if (y != null)
			{

				// CvPoint center = new CvPoint().x(x).y(y);
				xy.put(x, y.start);

				src.getGraphics().setColor(new Color(0, 255, 0));
				src.getGraphics().setPaintMode();
				src.getGraphics().drawRect(x - 2, y.start - 2, 4,
						 y.height + 2);
				// cvCircle(src, center, 5, CvScalar.GREEN, -1, 8, 0);
			}
		}

		// System.out.println("Elapsed " + (System.currentTimeMillis() -
		// start));
		return xy;
	}

	enum State
	{
		NONE, UP, DOWN
	}

	class Block
	{
		int start;
		int height;

		// block of correctHeight value = 1.0
		public double correctHeight;
	}

	/**
	 * find the brightest pixel that matches the "red laser hue" and has the
	 * required colorStep brightness change with regard to the previous pixel.
	 * 
	 * Additionally allow saturated pixels (255 in red channel) as happens when
	 * objects get close to the camera
	 * 
	 * @param rgbArray
	 * @param x
	 * @return
	 */
	private Block scanForPoint(FastRGB fastRGB, int x, int height)
	{

		for (int y = 0; y < height; y++)
		{
			// get line height for y
			int range = Math.max(getExpectedLineHeightForY(y),1);
			if (y - 3 >= 0 && (y + range + 3 < height))
			{
				// get average rgb above, below and in the line

				RGBData above = getAverageRGB(fastRGB, x, y -3, y );
				RGBData below = getAverageRGB(fastRGB, x, y + range, y
						+ range + 3);
				RGBData line = getAverageRGB(fastRGB, x, y , y
						+ range);

				// how much brighter the red channel is expected to be
				int expectedShift = 30;
				if (above.isLike(below, 10))
				{
					System.out.println("Above.isLike Below");
					if (!above.isLike(line,10))
					{
						System.out.println("Linke is not like above");
					}
				}
				
				if (above.isLike(below, 10) && !above.isLike(line, 10)
						&& !below.isLike(line, 10)
						&& line.r > above.r + (expectedShift))
				{
					Block block = new Block();
					block.start = y ;
					block.height = range;
					block.correctHeight = 1;
					return block;
				}
			}
		}

		return null;
	}

	class RGBData
	{
		int r;
		int b;
		int g;
		double samples;

		public boolean isLike(RGBData below, double tollerance)
		{
			double d1 = Math.abs((r / samples) - (below.r / below.samples));
			double d2 = Math.abs((b / samples) - (below.b / below.samples));
			double d3 = Math.abs((g / samples) - (below.g / below.samples));

			return d1 < tollerance && d2 < tollerance && d3 < tollerance;
		}
	}

	private RGBData getAverageRGB(FastRGB fastRGB, int x, int y1, int y2)
	{
		Preconditions.checkArgument(y1<y2);
		RGBData data = new RGBData();
		for (int y = y1; y < y2; y++)
		{
			int rgb = fastRGB.getRGB(x, y);
			Color color = new Color(rgb);
			data.r += color.getRed();
			data.g += color.getBlue();
			data.b += color.getGreen();
		}
		data.samples = y2 - y1;

		return data;
	}

	private int getExpectedLineHeightForY(int location)
	{
		// System.out.println("Expected height from "+location+" "+(coordResolver.getExpectedLineHeight(location)-location));
		return (int) Math
				.max(1,
						(coordResolver.getExpectedLineHeight(location) ) + 0.5d);
	}

	/**
	 * check that the color matches the "red laser" hue, with in the specified
	 * tolerance
	 * 
	 * @param tolerance
	 * @param rb
	 *            red / blue
	 * @param rg
	 *            reg /green
	 * @param bg
	 *            blue/green
	 * @return
	 */
	private boolean checkHue(double tolerance, double rb, double rg, double bg)
	{
		return Math.abs(rb - 1.20) < tolerance
				&& Math.abs(rg - 1.31) < tolerance
				&& Math.abs(bg - 1.08) < tolerance;
	}

}
