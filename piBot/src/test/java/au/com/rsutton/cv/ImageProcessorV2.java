package au.com.rsutton.cv;

import static org.bytedeco.javacpp.opencv_core.cvCircle;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bytedeco.javacpp.opencv_core.CvPoint;
import org.bytedeco.javacpp.opencv_core.CvScalar;
import org.bytedeco.javacpp.opencv_core.IplImage;

public class ImageProcessorV2
{
	public void processImage(final IplImage src)
	{

		// Flip upside down
		// cvFlip(src, src, 0);
		// Swap red and blue channels
		// cvCvtColor(src, src, CV_BGR2GRAY);

		long start = System.currentTimeMillis();
		BufferedImage bufferedImage = src.getBufferedImage();

		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();

		int MAX_LINE_HEIGHT = height / 100;

		int yStepSize = 1;

		for (int x = 5; x < width - 10; x += width / 100)
		{
			List<Point> top = scanForPoint(src, bufferedImage, 0, height, x,
					yStepSize);
			for (Point point : top)
			{
				CvPoint center = new CvPoint().x(point.x).y(point.y);

				cvCircle(src, center, 10, CvScalar.GREEN, -1, 8, 0);

			}

		}

		System.out.println("Elapsed " + (System.currentTimeMillis() - start));
	}

	private List<Point> scanForPoint(final IplImage src,
			BufferedImage bufferedImage, int startY, int endY, int x,
			int stepSize)
	{
		int maxRed = 0;
		int totalRed = 0;
		int step = (int) Math.signum(endY - startY) * stepSize;
		List<Point> brightestPoints = new LinkedList<>();

		for (int y = startY; (startY < endY && y < endY)
				|| (endY < startY && y > endY); y += step)
		{

			Color color = new Color(bufferedImage.getRGB(x - 3, y));

			int red = color.getRed();
			if ((brightestPoints.size() == 0 || red > brightestPoints.get(0).red)
					&& red > color.getBlue() * 1.3d
					&& red > color.getGreen() * 1.3d)
			{

				if (brightestPoints.size() > 0)
				{
					for (int i = brightestPoints.size()-1; i >= 0; i--)
					{
						if (brightestPoints.get(i).red < red)
						{
							brightestPoints.add(i, new Point(x, y, red));
							if (brightestPoints.size() > 10)
							{
								brightestPoints.remove(0);
							}
						}
					}
				}else
				{
					brightestPoints.add(new Point(x,y,red));
				}
				maxRed = red;
			}
			totalRed += red;
		}

		Iterator<Point> itr = brightestPoints.iterator();
		while (itr.hasNext())
		{
			int red = itr.next().red;
			if (red < (totalRed * 1.3d / Math.abs(startY - endY)))
			{
				itr.remove();
			}
		}
		return brightestPoints;
	}

	class Point
	{
		int x;
		int y;
		int red;

		public Point(int x2, int y2, int red2)
		{
			x = x2;
			y = y2;
			red = red2;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + red;
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Point other = (Point) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (red != other.red)
				return false;
			return true;
		}

		private ImageProcessorV2 getOuterType()
		{
			return ImageProcessorV2.this;
		}
	}
}
