package au.com.rsutton.robot.rover;

import java.io.Serializable;

public class Angle implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	AngleUnits internalUnits = AngleUnits.DEGREES;
	double angle = 0;

	public Angle(double angle, AngleUnits units)
	{
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
		return new Angle(this.angle + angle.convertTo(internalUnits),
				internalUnits);

	}

	Angle add(double angle, AngleUnits units)
	{
		return new Angle(this.angle + units.convertTo(angle, internalUnits),
				internalUnits);

	}

	Angle subtract(Angle angle)
	{
		return new Angle(this.angle - angle.convertTo(internalUnits),
				internalUnits);

	}

	Angle subtract(double angle, AngleUnits units)
	{
		return new Angle(this.angle - units.convertTo(angle, internalUnits),
				internalUnits);

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
		double ax = Math.cos(Math.toRadians(angle));
		double ay = Math.sin(Math.toRadians(angle));

		angle = Math.toDegrees(Math.atan2(ay, ax));
		if (angle < 0)
		{
			angle = 360 + angle;
		}

	}

	public double difference(double degrees, AngleUnits degrees2)
	{
		double diff = subtract(new Angle(degrees,degrees2)).getDegrees();
		
		if (diff>180)
		{
			diff = 360-diff;
		}
		
		return diff;
	}

	public double difference(Angle angle2)
	{
		return difference(angle2.getDegrees(),AngleUnits.DEGREES);
	}

}
