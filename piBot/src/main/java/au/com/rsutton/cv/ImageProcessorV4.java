package au.com.rsutton.cv;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import au.com.rsutton.mapping.CoordResolver;

public class ImageProcessorV4
{

	private static final int LINE_HEIGHT_TOLLERANCE = 5;
	double totalrb = 0;
	double totalrg = 0;
	double totalbg = 0;
	double count = 0;
	final CoordResolver coordResolver;

	public ImageProcessorV4(CoordResolver coordResolver)
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
			int y = scanForPoint(fastRGB, x, bufferedImage.getHeight());
			if (y > -1)
			{

				// CvPoint center = new CvPoint().x(x).y(y);
				xy.put(x, y);

				src.getGraphics().setColor(new Color(0, 255, 0));
				src.getGraphics().setPaintMode();
				src.getGraphics().drawRect(x - 2, y - 2, 4, 4);
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
	private Integer scanForPoint(FastRGB fastRGB, int x, int height)
	{
		Integer matchedY = -1;
		Integer matchedIntensity = 0;

		double hueTolerance = 0.20;
		int colorStep = 30;

		State state = State.NONE;
		int location = -1;

		double previousRed = new Color(fastRGB.getRGB(x, 2)).getRed();

		for (int y = 3; y < height; y++)
		{

			// int rgb = bufferedImage.getRGB(x, y);
			int rgb = fastRGB.getRGB(x, y);
			double red = new Color(rgb).getRed();

			double b2 = Math.max(1, new Color(rgb).getBlue());

			double g2 = Math.max(1, new Color(rgb).getGreen());

			double rb = red / b2;
			double rg = red / g2;
			double bg = b2 / g2;

			if (red > previousRed + colorStep)
			{
				// sufficently large step in red
				if (checkHue(hueTolerance, rb, rg, bg) || isPixelSaturated(red))
				{
					// matched hue or are saturated
					state = State.UP;
					location = y;
				}

			} else if (red < previousRed - colorStep)
			{
				// large step down in red
				if (state == State.UP
						&& Math.abs(compareHeightToExpectedHeight(location, y)) < LINE_HEIGHT_TOLLERANCE)
				{
					matchedY = y;
				}
				state = State.NONE;
			} else if (state == State.UP)
			{
				if (compareHeightToExpectedHeight(location, y) > LINE_HEIGHT_TOLLERANCE)
				{
					// we went pass the maximum possible line height
					state = State.NONE;
				} else if (!checkHue(hueTolerance, rb, rg, bg)
						&& !isPixelSaturated(red))
				{
					// we lost hue/saturation
					state = State.NONE;
				}
			}
			previousRed = red;
		}

		return matchedY;
	}

	private int compareHeightToExpectedHeight(int location, int y)
	{
		int difference = getExpectedLineHeightForY(location)-getHeight(location, y) ;
		System.out.println("difference between line height and expected line height "+difference);
		return difference;
	}

	private int getHeight(int location, int y)
	{
		return y - location;
	}

	private int getExpectedLineHeightForY(int location)
	{
	//	System.out.println("Expected height from "+location+" "+(coordResolver.getExpectedLineHeight(location)-location));
		return (int) Math.max(1,
				(coordResolver.getExpectedLineHeight(location)-location) + 0.5d);
	}

	private boolean isPixelSaturated(double r2)
	{
		return r2 == 255;
	}

	private void collectHueStatistics(double rb, double rg, double bg)
	{
		// totalrb += rb;
		// totalrg += rg;
		// totalbg += bg;
		// count++;
	}

	/**
	 * check that there is a change in color strength greater than colorStep
	 * between these 2 pixels
	 * 
	 * @param colorStep
	 * @param r1
	 * @param r2
	 * @return
	 */
	private boolean checkColorStep(int colorStep, double r1, double r2)
	{
		return r1 + colorStep < r2;
	}

	/**
	 * check that this pixel is brighter than any previously seen pixel
	 * 
	 * @param matchedIntensity
	 * @param r2
	 * @param b2
	 * @param g2
	 * @return
	 */
	private boolean checkIntensity(Integer matchedIntensity, double r2,
			double b2, double g2)
	{
		return matchedIntensity < r2 + b2 + g2;
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
