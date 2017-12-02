package au.com.rsutton.units;


public enum DistanceUnit
{
	MM(1), CM(10), M(1000), KM(1000000);

	private double scaler;

	DistanceUnit(double scaler)
	{
		this.scaler = scaler;
	}


	
	public double convert(double ret, DistanceUnit unit)
	{
		return ret * (this.scaler / unit.scaler);

	}
}
