package au.com.rsutton.entryPoint.trig;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;

public class TrigMath
{

	static public Distance distanceBetween(Point p1, Point p2)
	{
		// H = SQR(A^2 + B^2)
		double a2 = Math.pow(
				p1.x.convert(DistanceUnit.MM) - p2.x.convert(DistanceUnit.MM),
				2);

		double b2 = Math.pow(
				p1.y.convert(DistanceUnit.MM) - p2.y.convert(DistanceUnit.MM),
				2);

		Distance h = new Distance(Math.sqrt(a2 + b2), DistanceUnit.MM);
		return h;
	}

	/**
	 * determine if 3 points for a line, with a given tolerance.
	 * @param p1
	 * @param p2
	 * @param p3
	 * @param tolerance - 0.1 = 10%
	 * @return
	 */
	static public Boolean pointsFormLine(Point p1, Point p2, Point p3,
			double tolerance)
	{
		// calculate distances between each of the points
		double d1 = Math.abs(distanceBetween(p1, p2).convert(DistanceUnit.MM));
		double d2 = Math.abs(distanceBetween(p2, p3).convert(DistanceUnit.MM));
		double d3 = Math.abs(distanceBetween(p1, p3).convert(DistanceUnit.MM));

		// determine the farthest apart 2 points
		double b;
		double a;
		double ac;
		if (d1 > d2)
		{
			if (d1 > d3)
			{
				ac = d1;
				a = d2;
				b = d3;
			} else
			{
				ac = d3;
				a = d2;
				b = d1;
			}
		} else if (d2 > d3)
		{
			ac = d2;
			a = d1;
			b = d3;
		} else
		{
			ac = d3;
			a = d2;
			b = d1;
		}

		// the sum of a+b should equal ac
		return a + b < (ac + (ac * tolerance));
	}
}
