package au.com.rsutton.mapping.v3.linearEquasion;

import au.com.rsutton.mapping.XY;
import au.com.rsutton.mapping.v3.impl.ObservedPoint;
import au.com.rsutton.robot.rover.Angle;
import au.com.rsutton.robot.rover.AngleUnits;

public class LinearEquasionNormal implements LinearEquasion
{

	private double m;
	private double c;
	private Angle angle;

	// y = mx+c

	LinearEquasionNormal(ObservedPoint p1, ObservedPoint p2)
	{
		double run = p2.getX() - p1.getX();
		double rise = p2.getY() - p1.getY();
		m = rise / run;

		angle = new Angle(Math.toDegrees(Math.atan(m)), AngleUnits.DEGREES);

		// y=mx+c
		// y-mx = c;
		// c= y-mx;
		// p1.getY()=ratio*p1.getX()+c;
		//
		c = p1.getY() - (m * p1.getX());
	}

	public double getM()
	{
		return m;
	}

	public double getC()
	{
		return c;
	}

	public double getY(double x)
	{
		return (m * x) + c;
	}

	public double getX(double y)
	{
		// y = mx+c
		// mx = y-c
		// x = (y-c)/m
		return (y - c) / m;
	}

	@Override
	public boolean isSimilar(LinearEquasion otherLine,
			double angleTolleranceDegrees, double cTollerance, XY at)
	{
		boolean ret = false;

		ret = Math.abs(otherLine.getAngle()
				.difference(otherLine.getAngle())) <= angleTolleranceDegrees;
		if (otherLine instanceof LinearEquasionNormal)
		{
			ret &= Math.abs(((LinearEquasionNormal) otherLine).getX(at.getY())
					- at.getX()) <= cTollerance;
		} else if (otherLine instanceof VerticalLine)
		{
			ret &= Math.abs(((VerticalLine) otherLine).getX() - at.getX()) < cTollerance;

		} else if (otherLine instanceof HorizontalLine)
		{
			ret &= Math.abs(((HorizontalLine) otherLine).getY() - at.getY()) < cTollerance;

		}
		return ret;
	}

	@Override
	public InterceptResult getIntercept(LinearEquasion otherLine)
	{
		if (otherLine instanceof VerticalLine)
		{
			double otherX = ((VerticalLine) otherLine).getX();
			return new InterceptResult(InterceptType.INTERCEPT, new XY(
					(int) otherX, (int) getY(otherX)));
		} else if (otherLine instanceof HorizontalLine)
		{
			int otherY = (int) ((HorizontalLine) otherLine).getY();
			return new InterceptResult(InterceptType.INTERCEPT, new XY(
					(int) getX(otherY), otherY));

		} else
		{
			LinearEquasionNormal l2 = (LinearEquasionNormal) otherLine;

			double a = m;
			double b = l2.m;
			double d = l2.c;

			// y = ax+c and y = bx+d
			// Point of Intersection is {(d-c)/(a-b), (a*(d-c)/(a-b))+c}

			if (Math.abs(a - b) < 0.01)
			{
				// paralle lines
				if (Math.abs(c - d) < 0.01)
				{
					return new InterceptResult(InterceptType.SAME_LINE);
				} else
				{
					return new InterceptResult(InterceptType.NONE);
				}
			}

			double xIntercept = (d - c) / (a - b);
			double yIntercept = (a * xIntercept) + c;

			return new InterceptResult(InterceptType.INTERCEPT, new XY(
					(int) xIntercept, (int) yIntercept));
		}
	}

	@Override
	public Angle getAngle()
	{
		return angle;
	}

}
