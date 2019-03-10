package au.com.rsutton.depthcamera;

public class PeakFinder
{

	public double[][] findPeaks(double[][] image, double expectedDeviation, double voidValue)
	{
		double[][] result = new double[image.length][image[0].length];

		for (int x = 0; x < image.length; x++)
		{
			for (int y = 0; y < image[0].length; y++)
			{
				if (x >= 0 && y >= 0 && x <= image.length - 1 && y <= image[0].length - 1)
					if (isOutlier(x, y, image, expectedDeviation, voidValue))
					{
						result[x][y] = 1;
					}
			}
		}

		return result;
	}

	private boolean isOutlier(int x, int y, double[][] image, double expectedDeviation, double voidValue)
	{

		double center = image[x][y];
		if (Math.abs(center - voidValue) < 0.01)
		{
			return false;
		}

		double n = findNonVoid(center, x, y, image, 0, 1, voidValue);
		double s = findNonVoid(center, x, y, image, 0, -1, voidValue);
		double e = findNonVoid(center, x, y, image, 1, 0, voidValue);
		double w = findNonVoid(center, x, y, image, -1, 0, voidValue);

		// System.out.println(x + " " + y + " " + n + " " + s + " " + e + " " +
		// w);

		double ne = findNonVoid(center, x, y, image, 1, 1, voidValue);
		double sw = findNonVoid(center, x, y, image, -1, -1, voidValue);
		double se = findNonVoid(center, x, y, image, 1, -1, voidValue);
		double nw = findNonVoid(center, x, y, image, -1, 1, voidValue);

		return isOutlier(center, n, s, expectedDeviation) || isOutlier(center, e, w, expectedDeviation)
				|| isOutlier(center, ne, sw, expectedDeviation) || isOutlier(center, se, nw, expectedDeviation);

	}

	private double findNonVoid(double referenceValue, int x, int y, double[][] image, int xDirection, int yDirection,
			double voidValue)
	{
		boolean done = false;
		int steps = 0;
		while (done == false)
		{
			steps++;

			int xp = x + (xDirection * steps);
			int yp = y + (yDirection * steps);
			if (xp < 0 || yp < 0 || xp > image.length - 1 || yp > image[0].length - 1)
			{
				return referenceValue;
			}

			double value = image[xp][yp];
			if (Math.abs(value - voidValue) > 0.01)
			{
				// apportion the value by how the number of steps from the
				// referenceValue
				return referenceValue + ((value - referenceValue) / steps);
			}
		}
		return voidValue;
	}

	private boolean isOutlier(double center, double p1, double p2, double expectedDeviation)
	{
		double expectedValue = (p1 + p2) / 2.0;
		return Math.abs(center - expectedValue) > expectedDeviation;

	}

}
