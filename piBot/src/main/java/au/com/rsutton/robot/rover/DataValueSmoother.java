package au.com.rsutton.robot.rover;

import com.google.common.base.Preconditions;

public class DataValueSmoother
{

	Double lastValue = null;
	final double stability ;

	DataValueSmoother(double stability)
	{
		Preconditions.checkArgument(stability <= 1.0d && stability >= 0.0d,
				"Valid range for stability is between 0 & 1");
		this.stability = stability;
	}

	double smooth(double value)
	{
		if (lastValue == null)
		{
			lastValue = value;
		}
		double ret = (value * (1.0d - stability)) + (lastValue * stability);
		lastValue = ret;
		return ret;
	}
}
