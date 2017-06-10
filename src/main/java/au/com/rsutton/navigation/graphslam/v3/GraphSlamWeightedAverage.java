package au.com.rsutton.navigation.graphslam.v3;

public class GraphSlamWeightedAverage
{
	private double valueAccumulator;
	private double weightAccumulator;

	void addValue(GraphSlamWeightedAverage average)
	{
		valueAccumulator += average.valueAccumulator;
		weightAccumulator += average.weightAccumulator;
	}

	void addValue(double value, double weight)
	{
		valueAccumulator += (value * weight);
		weightAccumulator += weight;
	}

	double getWeightedAverage()
	{
		if (Math.abs(weightAccumulator) < 0.001)
		{
			return 0;
		}
		return valueAccumulator / weightAccumulator;
	}

	double getWeight()
	{
		return weightAccumulator;
	}

	@Override
	public String toString()
	{
		return "GraphSlamWeightedAverage [valueAccumulator=" + valueAccumulator + ", weightAccumulator="
				+ weightAccumulator + ", getWeightedAverage()=" + getWeightedAverage() + "]";
	}
}
