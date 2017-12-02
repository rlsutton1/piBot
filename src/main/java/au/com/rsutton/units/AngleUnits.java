package au.com.rsutton.units;

import java.io.Serializable;

public enum AngleUnits implements Serializable
{
	DEGREES
	{
		@Override
		public double convertTo(double angle, AngleUnits units)
		{
			if (units == this)
			{
				return angle;
			}
			return Math.toRadians(angle);
		}
	},
	RADIANS
	{
		@Override
		public double convertTo(double angle, AngleUnits units)
		{
			if (units ==this)
			{
				return angle;
			}
			return Math.toDegrees(angle);
		}
	};

	abstract public double convertTo(double angle, AngleUnits units);
}
