package au.com.rsutton.navigation.graphslam.v3;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class GraphSlamNodeLinear implements GraphSlamNode
{
	static final AtomicInteger nodeIdSeed = new AtomicInteger();

	int id = nodeIdSeed.incrementAndGet();
	protected double position;
	Map<GraphSlamNode, GraphSlamConstraint> constraints = new HashMap<>();
	double currentError;
	boolean isRoot = false;
	String name;

	GraphSlamNodeLinear(String name, double initialPosition)
	{
		position = initialPosition;
		this.name = name;

	}

	@Override
	public String toString()
	{
		return "Node [name=" + name + ", id=" + id + ", position=" + getPosition() + ", isRoot=" + isRoot + "]";
	}

	@Override
	public double calculateError(GraphSlamConstraint constraint)
	{
		return (position - constraint.node.getPosition()) + constraint.getOffset();
	}

	@Override
	public double getPosition()
	{
		return position;
	}

	@Override
	public void addConstraint(GraphSlamNode node, double offset, double certainty, ConstraintOrigin constraintDirection)
	{
		GraphSlamConstraint target = constraints.get(node);
		if (target != null)
		{
			if (target.constraintOrigin != constraintDirection)
			{
				throw new RuntimeException("Constraint Directions don't match");
			}

			target.addValue(offset, certainty);
		} else
		{
			constraints.put(node, new GraphSlamConstraint(this, node, offset, certainty, constraintDirection));
		}
	}

	@Override
	public void setIsRoot(boolean isRoot)
	{
		this.isRoot = isRoot;
	}

	@Override
	public boolean isRoot()
	{
		return isRoot;
	}

	@Override
	public Collection<GraphSlamConstraint> getConstraints()
	{
		return constraints.values();
	}

	@Override
	public void adjustPosition(double error)
	{
		position -= error;
	}

	@Override
	public void setCurrentError(double error)
	{
		currentError = error;
	}

	@Override
	public double getCurrentError()
	{
		return currentError;
	}

	@Override
	public double getNormalisedOffset(double offset)
	{
		return offset;

	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GraphSlamNodeLinear other = (GraphSlamNodeLinear) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public double getWeight()
	{
		double weight = 0;
		for (GraphSlamConstraint constraint : constraints.values())
		{
			weight += constraint.getWeight();
		}
		return weight;
	}

	@Override
	public void deleteConstraint(GraphSlamNode node)
	{
		constraints.remove(node);
	}

}