package au.com.rsutton.mapping.v3;

public class ValueWithError
{

	@Override
	public String toString()
	{
		return "ValueWithError [value=" + value + ", error=" + error + "]";
	}

	final private double value;
	final private double error;

	public ValueWithError(double value, double error)
	{
		this.value = value;
		this.error = error;
	}

	public double getValue()
	{
		return value;
	}

	public double getError()
	{
		return error;
	}

}
