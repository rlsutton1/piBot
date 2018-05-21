package au.com.rsutton.navigation.graphslam.v3;

public class WeightedPose<J>
{
	private J pose;
	private double weight;

	public WeightedPose(J pose, double weight)
	{
		this.pose = pose;
		this.weight = weight;
	}

	public double getWeight()
	{
		return weight;
	}

	public J getPose()
	{
		return pose;
	}
}
