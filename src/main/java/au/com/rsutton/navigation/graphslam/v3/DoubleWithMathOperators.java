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

	List<Double> valuesForAverage = new LinkedList<>();
	double totalWeight = 0;

	@Override
	public void addWeightedValueForAverage(DoubleWithMathOperators value, double weight)
	{
		valuesForAverage.add(value.getValue());
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
		return new DoubleWithMathOperators(total / Math.max(1, valuesForAverage.size()));
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

	public double getValue()
	{
		return value;
	}

	@Override
	public void dumpObservations()
	{
		for (Double value : valuesForAverage)
		{
			logger.error("--------> Observation " + value);
		}

	}

}
