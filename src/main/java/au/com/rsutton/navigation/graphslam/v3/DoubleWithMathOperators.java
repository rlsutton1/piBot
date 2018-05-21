package au.com.rsutton.navigation.graphslam.v3;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;

public class DoubleWithMathOperators implements MathOperators<DoubleWithMathOperators>
{

	private final double value;
	private org.apache.logging.log4j.Logger logger = LogManager.getLogger();

	DoubleWithMathOperators(double value)
	{
		this.value = value;
	}

	@Override
	public DoubleWithMathOperators applyOffset(DoubleWithMathOperators value)
	{
		return new DoubleWithMathOperators(this.value + value.getValue());
	}

	@Override
	public DoubleWithMathOperators plus(DoubleWithMathOperators value)
	{
		return new DoubleWithMathOperators(this.value + value.getValue());
	}

	@Override
	public DoubleWithMathOperators minus(DoubleWithMathOperators value)
	{
		return new DoubleWithMathOperators(this.value - value.getValue());
	}

	@Override
	public String toString()
	{

		return "" + value;
	}

	List<WeightedPose<DoubleWithMathOperators>> valuesForAverage = new LinkedList<>();

	@Override
	public void addWeightedValueForAverage(WeightedPose<DoubleWithMathOperators> value)
	{
		valuesForAverage.add(value);

	}

	@Override
	public DoubleWithMathOperators getWeightedAverage()
	{
		double total = 0;
		for (WeightedPose<DoubleWithMathOperators> value : valuesForAverage)
		{
			total += value.getPose().getValue();
		}
		return new DoubleWithMathOperators(total / Math.max(1, valuesForAverage.size()));
	}

	@Override
	public DoubleWithMathOperators copy()
	{
		return new DoubleWithMathOperators(value);
	}

	public double getValue()
	{
		return value;
	}

	@Override
	public void dumpObservations()
	{
		for (WeightedPose<DoubleWithMathOperators> value : valuesForAverage)
		{
			logger.error("--------> Observation " + value.getPose().getValue() + " W:" + value.getWeight());
		}

	}

	@Override
	public double getWeight()
	{
		double total = 0;
		for (WeightedPose<DoubleWithMathOperators> value : valuesForAverage)
		{
			total += value.getWeight();
		}
		return total;

	}

	@Override
	public DoubleWithMathOperators multiply(double scaler)
	{
		return new DoubleWithMathOperators(value * scaler);
	}

	// @Override
	// public DoubleWithMathOperators inverse()
	// {
	// return new DoubleWithMathOperators(-value);
	// }

}
