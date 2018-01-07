package au.com.rsutton.units;

import java.io.Serializable;

import com.google.common.base.Preconditions;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;

public class Angle implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	AngleUnits internalUnits = AngleUnits.DEGREES;
	double angle = 0;

	public Angle()
	{

	}

	public Angle(double angle, AngleUnits units)
	{
		Preconditions.checkArgument(Double.NaN != angle, "Nan angle");
		this.angle = units.convertTo(angle, internalUnits);
		rationalizeAngle();
	}

	public Angle(Angle angle2)
	{
		this.angle = angle2.angle;

	}

	private double convertTo(AngleUnits units)
	{
		return internalUnits.convertTo(angle, units);
	}

	Angle add(Angle angle)
	{
		return new Angle(this.angle + angle.convertTo(internalUnits), internalUnits);

	}

	Angle add(double angle, AngleUnits units)
	{
		return new Angle(this.angle + units.convertTo(angle, internalUnits), internalUnits);

	}

	Angle subtract(Angle angle)
	{
		return new Angle(this.angle - angle.convertTo(internalUnits), internalUnits);

	}

	Angle subtract(double angle, AngleUnits units)
	{
		return new Angle(this.angle - units.convertTo(angle, internalUnits), internalUnits);

	}

	public double getDegrees()
	{
		return convertTo(AngleUnits.DEGREES);

	}

	public double getRadians()
	{
		return convertTo(AngleUnits.RADIANS);
	}

	private void rationalizeAngle()
	{
		angle = HeadingHelper.normalizeHeading(angle);

	}

	/**
	 * always returns a positive value
	 * 
	 * @param degrees
	 * @param units
	 * @return
	 */
	public double difference(double degrees, AngleUnits units)
	{
		double diff = subtract(new Angle(degrees, units)).getDegrees();

		if (diff > 180)
		{
			diff = 360 - diff;
		}

		return diff;
	}

	/**
	 * always returns a positive value
	 * 
	 * @param angle2
	 * @return
	 */
	public double difference(Angle angle2)
	{
		return difference(angle2.getDegrees(), AngleUnits.DEGREES);
	}

}
