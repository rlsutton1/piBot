package au.com.rsutton.entryPoint.sonar;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;

public class SharpIR
{

	public SharpIR(double scalar, int baseOffset, double linearize)
	{
	}

	public Distance getCurrentDistance(int rawValue)
	{

		double r = rawValue;

		// this I came up with after collecting raw data, putting it into excel
		// and getting it to do a
		// trend line. then rearranging the formula.
		double distance = Math.pow(Math.E, ((r - 17488) / -2768));

		distance = distance *10;
		
		return new Distance(distance, DistanceUnit.MM);
	}

}
