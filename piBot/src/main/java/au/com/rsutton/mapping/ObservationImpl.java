package au.com.rsutton.mapping;

public class ObservationImpl implements Observation
{

	double x;
	double y;
	double accuracy;

	// if it's not an object it must be clear space
	boolean isObject = true;
	
	ObservationImpl(double x, double y, double accuracy)
	{
		this.x= x;
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
	public boolean isObject()
	{
		return isObject;
	}
}
