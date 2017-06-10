package au.com.rsutton.navigation.graphslam.v3;

import java.util.Collection;

public interface GraphSlamNode
{
	double calculateError(GraphSlamConstraint constraint);

	public double getPosition();

	void setIsRoot(boolean isRoot);

	boolean isRoot();

	Collection<GraphSlamConstraint> getConstraints();

	void adjustPosition(double error);

	void setCurrentError(double error);

	double getCurrentError();

	double getNormalisedOffset(double offset);

	void addConstraint(GraphSlamNode node, double offset, double certainty, ConstraintOrigin constraintDirection);

	double getWeight();

	void deleteConstraint(GraphSlamNode node);

}
