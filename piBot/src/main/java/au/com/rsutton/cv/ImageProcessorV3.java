package au.com.rsutton.cv;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class ImageProcessorV3
{
	public Map<Integer,Integer> processImage(final BufferedImage src)
	{
		// first pass with wide threshold (1.3)
		return processImageInternal(src, 1.3d);

		// second pass with tight threshold (1.4) as the first pass removed most
		// of
		// the matches, there is a much
		// greater risk of introducing noise now!!
	//	processImageInternal(src, 1.4d);
	}

	public Map<Integer,Integer> processImageInternal(final BufferedImage src, double threshold)
	{
		Map<Integer,Integer> xy=new  HashMap<>();

		// Flip upside down
		// cvFlip(src, src, 0);
		// Swap red and blue channels
		// cvCvtColor(src, src, CV_BGR2GRAY);

		long start = System.currentTimeMillis();
		BufferedImage bufferedImage = src;

		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();

		int MAX_LINE_HEIGHT = height / 100;

		int yStepSize = 1;

		int[] lastYCenter = new int[3];
		for (int x = 5; x < width - 10; x += width / 100)
		{
			int top = scanForPoint(src, bufferedImage, 0, height - 5, x,
					yStepSize, threshold);
			if (top > -1)
			{
				int bottom = scanForPoint(src, bufferedImage, height - 5, top,
						x, yStepSize, threshold);

				if (bottom > -1 && Math.abs(top - bottom) < MAX_LINE_HEIGHT)
				{
					// only accept the point if it is within y+-20 of the
					// previous point. This should eliminate noise!!!
					int averageLast = (lastYCenter[0] + lastYCenter[1] + lastYCenter[2]) / 3;
					if (Math.abs(((top + bottom) / 2) - averageLast) < 10)
					{
//						CvPoint center = new CvPoint().x(x).y(top);
//
//						cvCircle(src, center, 10, CvScalar.GREEN, -1, 8, 0);
//						center = new CvPoint().x(x).y(bottom);
//
//						cvCircle(src, center, 10, CvScalar.GREEN, -1, 8, 0);
						
						xy.put(x, ((top + bottom) / 2));
					}
					lastYCenter[0] = lastYCenter[1];
					lastYCenter[1] = lastYCenter[2];
					lastYCenter[2] = (top + bottom) / 2;
				}
			}

		}

		System.out.println("Elapsed " + (System.currentTimeMillis() - start));
		return xy;
	}

	private int scanForPoint(final BufferedImage src, BufferedImage bufferedImage,
			int startY, int endY, int x, int stepSize, double threshold)
	{
		int location = 0;
		int maxRed = 0;
		int totalRed = 0;
		int step = (int) Math.signum(endY - startY) * stepSize;

		for (int y = startY; (startY < endY && y < endY)
				|| (endY < startY && y > endY); y += step)
		{

			Color[] colorsx = new Color[3];
			colorsx[0] = new Color(bufferedImage.getRGB(x - 3, y));
			colorsx[1] = new Color(bufferedImage.getRGB(x, y));
			colorsx[2] = new Color(bufferedImage.getRGB(x + 3, y));

			int votes = 0;
			for (Color color : colorsx)
			{
				int red = color.getRed();
				if (red > maxRed
						&& red > color.getBlue() * threshold
						&& red > color.getGreen() * threshold)
				{
					votes++;
				}
			}

			int red = colorsx[1].getRed();

			if (votes == 3)
			{

				location = y;
				maxRed = red;
			}
			totalRed += red;
		}

		if (maxRed > (totalRed * threshold / Math.abs(startY - endY)))
		{

			return location;
			// System.out.println("Max " + maxRed + " y " + location);
		}
		return -1;
	}

}
