package au.com.rsutton.cv;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ImageProcessor
{
	public void processImage(final BufferedImage src)
	{

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

		for (int x = 5; x < width - 10; x += width / 100)
		{
			int top = scanForPoint(src, bufferedImage, 0, height, x, yStepSize);
			if (top > -1)
			{
				int bottom = scanForPoint(src, bufferedImage, height - 1, top,
						x, yStepSize);

				if (bottom > -1 && Math.abs(top - bottom) < MAX_LINE_HEIGHT)
				{
					//CvPoint center = new CvPoint().x(x).y(top);

					//cvCircle(src, center, 10, CvScalar.GREEN, -1, 8, 0);
					//center = new CvPoint().x(x).y(bottom);

					//cvCircle(src, center, 10, CvScalar.GREEN, -1, 8, 0);

				}
			}

		}

		System.out.println("Elapsed " + (System.currentTimeMillis() - start));
	}

	private int scanForPoint(final BufferedImage src, BufferedImage bufferedImage,
			int startY, int endY, int x, int stepSize)
	{
		int location = 0;
		int maxRed = 0;
		int totalRed = 0;
		int step = (int) Math.signum(endY - startY) * stepSize;

		for (int y = startY; (startY < endY && y < endY)
				|| (endY < startY && y > endY); y += step)
		{

			Color[] colors = new Color[3];
			colors[0] = new Color(bufferedImage.getRGB(x - 3, y));
			colors[1] = new Color(bufferedImage.getRGB(x, y));
			colors[2] = new Color(bufferedImage.getRGB(x + 3, y));

			int votes = 0;
			for (Color color : colors)
			{
				int red = color.getRed();
				if (red > maxRed && red > color.getBlue() * 1.3d
						&& red > color.getGreen() * 1.3d)
				{
					votes++;
				}
			}
			int red = colors[1].getRed();
			if (votes == 3)
			{

				location = y;
				maxRed = red;
			}
			totalRed += red;
		}

		if (maxRed > (totalRed * 1.1d / Math.abs(startY - endY)))
		{

			return location;
			// System.out.println("Max " + maxRed + " y " + location);
		}
		return -1;
	}

}
