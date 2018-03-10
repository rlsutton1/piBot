package au.com.rsutton.navigation.graphslam.v3;

public interface MathOperators<T>
{
	T applyOffset(T value);

	T plus(T value);

	void addWeightedValueForAverage(T value, double weight);

	T getWeightedAverage();

	T minus(T value);

	T copy();

	double getWeight();

	void dumpObservations();

}
