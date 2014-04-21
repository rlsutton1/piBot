package au.com.rsutton.mapping;

import com.google.common.base.Preconditions;

public class ObservationImpl implements Observation
{

	double x;
	double y;
	double accuracy;

	// if it's not an object it must be clear space
	LocationStatus status = LocationStatus.UNOBSERVED;
	private int seen = 1;

	ObservationImpl(double x, double y, double accuracy, LocationStatus status)
	{
		Preconditions.checkArgument(status != LocationStatus.UNOBSERVED,
				"Location status can not be UNOBSERVED");
		this.status = status;
		this.x = x;
		this.y = y;
		this.accuracy = accuracy;
	}

	@Override
	public double getAccuracy()
	{
		return accuracy;
	}

	@Override
	public double getX()
	{
		return x;
	}

	@Override
	public double getY()
	{
		return y;
	}

	@Override
	public void seenAgain()
	{
		seen++;

	}

	@Override
	public LocationStatus getStatus()
	{
		return status;
	}
}
