package au.com.rsutton.mapping.v3.linearEquasion;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.mapping.XY;
import au.com.rsutton.robot.rover.Angle;
import au.com.rsutton.robot.rover.AngleUnits;

public class VerticalLine implements LinearEquasion
{

	private double x;

	public VerticalLine(double x)
	{
		this.x = x;
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
			ret &= Math.abs(((LinearEquasionNormal) otherLine).getXFromY(at.getY())
					- at.getX()) <= cTollerance;
		} else if (otherLine instanceof VerticalLine)
		{
			ret &= Math.abs(((VerticalLine) otherLine).getX() - getX()) < cTollerance;

		} else if (otherLine instanceof HorizontalLine)
		{
			return false;
		}
		return ret;
	}

	@Override
	public InterceptResult getIntercept(LinearEquasion otherLine)
	{
		if (otherLine instanceof VerticalLine)
		{
			VerticalLine other = (VerticalLine) otherLine;
			if (Math.abs(x - other.x) < 1)
			{
				return new InterceptResult(InterceptType.SAME_LINE);
			} else
			{
				return new InterceptResult(InterceptType.NONE);
			}
		} else if (otherLine instanceof HorizontalLine)
		{
			HorizontalLine other = (HorizontalLine) otherLine;

			{
				return new InterceptResult(InterceptType.INTERCEPT, new XY(
						(int) getX(), (int) other.getY()));
			}

		}

		LinearEquasionNormal other = (LinearEquasionNormal) otherLine;
		return new InterceptResult(InterceptType.INTERCEPT, new XY((int) getX(),
				(int) other.getYFromX(getX())));

	}

	@Override
	public Angle getAngle()
	{
		return new Angle(0,AngleUnits.DEGREES);
	}

	public double getX()
	{
		return x;
	}
	
	@Override
	public boolean isPointOnLine(Vector3D point, double accuracy)
	{
		return (Math.abs(point.getX()-x) < accuracy);
	}

}
