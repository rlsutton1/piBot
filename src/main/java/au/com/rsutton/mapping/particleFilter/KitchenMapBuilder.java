package au.com.rsutton.mapping.particleFilter;

import au.com.rsutton.mapping.probability.ProbabilityMap;

public class KitchenMapBuilder
{

	public static ProbabilityMap buildKitchenMap()
	{
		ProbabilityMap world = new ProbabilityMap(10);

		// table leg
		drawLine(-120, 60, -120, 70, world);

		// bin
		// drawLine(-40,70,-10,70,world);
		// drawLine(-40,70,-40,40,world);
		// drawLine(-10,40,-10,70,world);
		// drawLine(-40,40,-10,40,world);

		drawLine(0, 0, 0, 89, world);// a
		drawLine(0, 0, -141, 0, world);// b
		drawLine(-141, 0, -141, 42, world);// c
		drawLine(-141, 42, -266, 42, world);// d

		drawLine(-266, 42, -266, 180, world);//
		drawLine(-266, 180, -194, 180, world);// e
		drawLine(-194, 180, -194, 252, world);// f
		drawLine(-194, 252, -223, 252, world);// g
		drawLine(-223, 252, -223, 407, world);// h
		drawLine(-223, 407, -49, 407, world);// i
		drawLine(-49, 407, -49, 325, world);// j
		drawLine(-49, 325, -57, 325, world);// k
		drawLine(-57, 325, -57, 170, world);// l
		drawLine(-57, 170, 0, 170, world);// m

		drawLine(-57, 170, 110, 170, world);// m
		drawLine(110, 170, 110, -86, world);// m

		drawLine(0, 0, 0, -346, world);// m

		drawLine(110, -86, 165, -86, world);// m
		drawLine(165, -86, 165, -346, world);// m
		drawLine(165, -346, 0, -346, world);// m

		world.dumpTextWorld();

		return world;

	}

	public static void drawLine(double x1, double y1, double x2, double y2, ProbabilityMap world)
	{
		if (x1 > x2)
		{
			double temp = x1;
			x1 = x2;
			x2 = temp;
		}

		if (y1 > y2)
		{
			double temp = y1;
			y1 = y2;
			y2 = temp;
		}

		double xd = x2 - x1;
		double yd = y2 - y1;
		if (Math.abs(0.0 - xd) < 0.000001)
		{
			double angle = (yd) / (xd);
			for (int y = (int) y1; y < y2; y++)
			{
				double x = y * angle;
				if (Double.isNaN(x) || Double.isInfinite(x) || Math.abs(0.0 - angle) < 0.001)
				{
					x = x1;
				}
				world.updatePoint((int) x, y, 1.0, 2);
			}

		}

		else
		{

			double angle = (xd) / (yd);
			for (int x = (int) x1; x < x2; x++)
			{
				double y = x * angle;
				if (Double.isNaN(y) || Double.isInfinite(y) || Math.abs(0.0 - angle) < 0.001)
				{
					y = y1;
				}
				world.updatePoint(x, (int) y, 1.0, 2);
			}
		}

	}

}
