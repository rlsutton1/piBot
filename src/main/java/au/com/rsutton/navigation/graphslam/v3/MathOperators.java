package au.com.rsutton.navigation.graphslam.v3;

public interface MathOperators<T>
{
	T applyOffset(T value);

	T plus(T value);

	void addWeightedValueForAverage(WeightedPose<T> value);

	T getWeightedAverage();

	double getWeight();

	T multiply(double scaler);

	T minus(T value);

	T copy();

	void dumpObservations();

	// T inverse();

}
