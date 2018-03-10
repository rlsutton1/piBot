package au.com.rsutton.navigation.graphslam.v3;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class GraphSlamNodeImpl<T extends MathOperators<T>> implements GraphSlamNode<T>
{
	private static final AtomicInteger nodeIdSeed = new AtomicInteger();

	private int id = nodeIdSeed.incrementAndGet();
	private T position;
	private Map<GraphSlamNode<T>, GraphSlamConstraint<T>> constraints = new HashMap<>();
	private T currentError;
	private boolean isRoot = false;
	private String name;

	private final T zero;

	GraphSlamNodeImpl(String name, T initialPosition, T zero)
	{
		position = initialPosition;
		this.zero = zero;
		this.name = name;

	}

	@Override
	public String toString()
	{
		return "Node [name=" + name + ", id=" + id + ", position=" + getPosition() + ", isRoot=" + isRoot + "]";
	}

	@Override
	public void addCalculatedError(GraphSlamConstraint<T> constraint)
	{
		T calculatedPosition = constraint.getParentNode().getPosition().applyOffset(constraint.getOffset());
		T error = calculatedPosition.minus(position);

		currentError.addWeightedValueForAverage(error, 1);
	}

	@Override
	public T getPosition()
	{
		return position;
	}

	@Override
	public void addConstraint(GraphSlamNode<T> node, T offset, double certainty)
	{
		GraphSlamConstraint<T> target = constraints.get(node);
		if (target != null)
		{
			target.addValue(offset, certainty);
		} else
		{
			constraints.put(node, new GraphSlamConstraint<>(this, node, offset, certainty, zero));
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
	public Collection<GraphSlamConstraint<T>> getConstraints()
	{
		return constraints.values();
	}

	@Override
	public void adjustPosition()
	{
		position = position.plus(currentError.getWeightedAverage());
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
		@SuppressWarnings("unchecked")
		GraphSlamNodeImpl<T> other = (GraphSlamNodeImpl<T>) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public double getWeight()
	{
		double weight = 0;
		for (GraphSlamConstraint<T> constraint : constraints.values())
		{
			weight += constraint.getWeight();
		}
		return weight;
	}

	@Override
	public void clearError()
	{
		currentError = zero.copy();

	}

}