package au.com.rsutton.units;

public class WeightedAngle
{
	double angle;
	double weight;

	public WeightedAngle(double angle, double weight)
	{
		this.angle = angle;
		this.weight = weight;
	}

	double getWeight()
	{
		return weight;
	}

	double getAngle()
	{
		return angle;
	}
}