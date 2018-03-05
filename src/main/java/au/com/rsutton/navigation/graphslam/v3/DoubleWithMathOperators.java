package au.com.rsutton.navigation.graphslam.v3;

import java.util.LinkedList;
import java.util.List;

public class DoubleWithMathOperators implements MathOperators<DoubleWithMathOperators>
{

	final double value;

	DoubleWithMathOperators(double value)
	{
		this.value = value;
	}

	@Override
	public DoubleWithMathOperators applyOffset(DoubleWithMathOperators value)
	{
		return new DoubleWithMathOperators(this.value + value.value);
	}

	@Override
	public DoubleWithMathOperators adjust(DoubleWithMathOperators value)
	{
		return new DoubleWithMathOperators(this.value + value.value);
	}

	@Override
	public DoubleWithMathOperators zero()
	{
		return new DoubleWithMathOperators(0);
	}

	@Override
	public DoubleWithMathOperators minus(DoubleWithMathOperators value)
	{
		return new DoubleWithMathOperators(this.value - value.value);
	}

	@Override
	public String toString()
	{
		return "" + value;
	}

	List<Double> valuesForAverage = new LinkedList<>();
	double totalWeight = 0;

	@Override
	public void addWeightedValueForAverage(DoubleWithMathOperators value, double weight)
	{
		valuesForAverage.add(value.value);
		totalWeight += weight;

	}

	@Override
	public DoubleWithMathOperators getWeightedAverage()
	{
		double total = 0;
		for (Double value : valuesForAverage)
		{
			total += value;
		}
		return new DoubleWithMathOperators(total / valuesForAverage.size());
	}

	@Override
	public DoubleWithMathOperators copy()
	{
		return new DoubleWithMathOperators(value);
	}

	@Override
	public double getWeight()
	{
		return totalWeight;

	}

	@Override
	public DoubleWithMathOperators inverse()
	{
		return new DoubleWithMathOperators(value * -1);
	}

}
