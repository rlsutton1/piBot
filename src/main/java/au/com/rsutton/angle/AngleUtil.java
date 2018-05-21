package au.com.rsutton.angle;

import java.util.List;

public class AngleUtil
{

	public static double delta(double angle1, double angle2)
	{
		double na1 = normalize(angle1);
		double na2 = normalize(angle2);
		return na2 - na1;
	}

	public static double getAverageAngle(List<WeightedAngle> angles)
	{
		double x = 0;
		double y = 0;
		for (WeightedAngle weightedAngle : angles)
		{
			x += Math.cos(Math.toRadians(weightedAngle.getAngle())) * weightedAngle.getWeight();
			y += Math.sin(Math.toRadians(weightedAngle.getAngle())) * weightedAngle.getWeight();
		}

		double result = normalize(Math.toDegrees(Math.atan2(y, x)));

		return result;
	}

	public static double normalize(double angle)
	{
		double result = angle;

		// round to 2 decimal places
		result = ((int) (result * 100)) / 100.0;
		if (result >= 360)
		{
			result = result % 360;
		}

		if (result < 0)
		{
			result = 360.0 + (result % 360);
		}
		return result;
	}

}
