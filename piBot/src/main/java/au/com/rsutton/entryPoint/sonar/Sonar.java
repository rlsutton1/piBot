package au.com.rsutton.entryPoint.sonar;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;

public class Sonar
{

	final private double scalar;
	final private int baseOffset;

	public Sonar(double scalar, int baseOffset)
	{
		this.scalar = scalar;
		this.baseOffset = baseOffset;

	}

	public Distance getCurrentDistance(int rawValue)
	{
		int distance = 0;

		distance = rawValue + baseOffset;

		distance *= scalar;

		return new Distance(distance, DistanceUnit.CM);
	}

}
