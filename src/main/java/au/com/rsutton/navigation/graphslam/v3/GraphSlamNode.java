package au.com.rsutton.navigation.graphslam.v3;

import java.util.Collection;

public interface GraphSlamNode<T extends MathOperators<T>>
{
	T calculateError(GraphSlamConstraint<T> constraint);

	public T getPosition();

	void setIsRoot(boolean isRoot);

	boolean isRoot();

	Collection<GraphSlamConstraint<T>> getConstraints();

	void adjustPosition(T error);

	void setCurrentError(T error);

	T getCurrentError();

	T getNormalisedOffset(T offset);

	void addConstraint(GraphSlamNode<T> node, T offset, double certainty, ConstraintOrigin constraintDirection);

	double getWeight();

	void deleteConstraint(GraphSlamNode<T> node);

}
