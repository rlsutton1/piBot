package au.com.rsutton.mapping.v3.linearEquasion;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.mapping.XY;
import au.com.rsutton.robot.rover.Angle;
import au.com.rsutton.robot.rover.AngleUnits;

public class HorizontalLine implements LinearEquasion
{

	private double y;

	public HorizontalLine(double y)
	{
		this.y = y;
	}

	@Override
	public boolean isSimilar(LinearEquasion otherLine,
			double angleTolleranceDegrees, double cTollerance, XY at)
	{
		boolean ret = false;

		ret = Math.abs(otherLine.getAngle().difference(otherLine.getAngle())) <= angleTolleranceDegrees;
		if (otherLine instanceof LinearEquasionNormal)
		{
			ret &= Math.abs(((LinearEquasionNormal) otherLine).getXFromY(at.getY())
					- at.getX()) <= cTollerance;
		} else if (otherLine instanceof VerticalLine)
		{
			return false;
		} else if (otherLine instanceof HorizontalLine)
		{
			ret &= Math.abs(((HorizontalLine) otherLine).getY() - getY()) < cTollerance;
		}
		return ret;
	}

	@Override
	public InterceptResult getIntercept(LinearEquasion otherLine)
	{
		if (otherLine instanceof HorizontalLine)
		{
			HorizontalLine other = (HorizontalLine) otherLine;
			if (Math.abs(y - other.y) < 1)
			{
				return new InterceptResult(InterceptType.SAME_LINE);
			} else
			{
				return new InterceptResult(InterceptType.NONE);
			}
		} else if (otherLine instanceof VerticalLine)
		{
			VerticalLine other = (VerticalLine) otherLine;

			{
				return new InterceptResult(InterceptType.INTERCEPT, new XY(
						(int) other.getX(), (int) getY()));
			}

		}

		LinearEquasionNormal other = (LinearEquasionNormal) otherLine;
		return new InterceptResult(InterceptType.INTERCEPT, new XY(
				(int) other.getXFromY(getY()), (int) getY()));

	}

	@Override
	public Angle getAngle()
	{
		return new Angle(90, AngleUnits.DEGREES);

	}

	public double getY()
	{
		return y;
	}

	@Override
	public boolean isPointOnLine(Vector3D point, double accuracy)
	{
		return (Math.abs(point.getY()-y) < accuracy);
	}

}
