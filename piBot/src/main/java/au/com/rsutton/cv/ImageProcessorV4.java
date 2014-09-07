package au.com.rsutton.cv;

import static org.bytedeco.javacpp.opencv_core.cvCircle;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import org.bytedeco.javacpp.opencv_core.CvPoint;
import org.bytedeco.javacpp.opencv_core.CvScalar;
import org.bytedeco.javacpp.opencv_core.IplImage;

public class ImageProcessorV4
{

	double totalrb = 0;
	double totalrg = 0;
	double totalbg = 0;
	double count = 0;

	public Map<Integer, Integer> processImage(final IplImage src)
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

	public Map<Integer, Integer> processImageInternal(final IplImage src,
			double threshold)
	{
		Map<Integer, Integer> xy = new HashMap<>();

		long start = System.currentTimeMillis();
		BufferedImage bufferedImage = src.getBufferedImage();

		int width = bufferedImage.getWidth();

		for (int x = 5; x < width - 10; x += width / 100)
		{
			int y = scanForPoint(bufferedImage, x);
			if (y > -1)
			{

				CvPoint center = new CvPoint().x(x).y(y);
				xy.put(x, y);

				cvCircle(src, center, 5, CvScalar.GREEN, -1, 8, 0);
			}
		}

		System.out.println("Elapsed " + (System.currentTimeMillis() - start));
		return xy;
	}

	/**
	 * find the brightest pixel that matches the "red laser hue" and has the
	 * required colorStep brightness change with regard to the previous pixel.
	 * 
	 * Additionally allow saturated pixels (255 in red channel) as happens when
	 * objects get close to the camera
	 * 
	 * @param bufferedImage
	 * @param x
	 * @return
	 */
	private Integer scanForPoint(BufferedImage bufferedImage, int x)
	{
		Integer matchedY = -1;
		Integer matchedIntensity = 0;

		double hueTolerance = 0.20;
		int colorStep = 30;

		for (int y = 3; y < bufferedImage.getHeight(); y++)
		{

			int r1 = new Color(bufferedImage.getRGB(x, y - 1)).getRed();
			double r2 = new Color(bufferedImage.getRGB(x, y)).getRed();

			double b2 = Math.max(1,
					new Color(bufferedImage.getRGB(x, y)).getBlue());

			double g2 = Math.max(1,
					new Color(bufferedImage.getRGB(x, y)).getGreen());

			double rb = r2 / b2;
			double rg = r2 / g2;
			double bg = b2 / g2;

			if (checkColorStep(colorStep, r1, r2) || isPixelSaturated(r2))
			{
				if (checkHue(hueTolerance, rb, rg, bg) || isPixelSaturated(r2))
				{
					collectHueStatistics(rb, rg, bg);
					if (checkIntensity(matchedIntensity, r2, b2, g2))
					{
						matchedY = y;
						matchedIntensity = (int) (r2 + b2 + g2);
					}
				}
			}
		}

		return matchedY;
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
	private boolean checkColorStep(int colorStep, int r1, double r2)
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
