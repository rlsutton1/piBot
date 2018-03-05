package au.com.rsutton.navigation.graphslam.v3;

public interface MathOperators<T>
{
	T applyOffset(T value);

	T adjust(T value);

	T inverse();
	//
	// T divideScaler(double scaler);

	void addWeightedValueForAverage(T value, double weight);

	T getWeightedAverage();

	T zero();

	T minus(T value);

	T copy();

	double getWeight();

}
