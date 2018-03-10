package au.com.rsutton.navigation.graphslam.v3;

import java.util.Collection;

public interface GraphSlamNode<T extends MathOperators<T>>
{
	void addCalculatedError(GraphSlamConstraint<T> constraint);

	public T getPosition();

	void setIsRoot(boolean isRoot);

	boolean isRoot();

	Collection<GraphSlamConstraint<T>> getConstraints();

	void adjustPosition();

	void addConstraint(GraphSlamNode<T> node, T offset, double certainty);

	double getWeight();

	void clearError();

}
