package au.com.rsutton.navigation.graphslam.v3;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class GraphSlamNodeImpl<T extends MathOperators<T>> implements GraphSlamNode<T>
{
	static final AtomicInteger nodeIdSeed = new AtomicInteger();

	int id = nodeIdSeed.incrementAndGet();
	protected T position;
	Map<GraphSlamNode<T>, GraphSlamConstraint<T>> constraints = new HashMap<>();
	T currentError;
	boolean isRoot = false;
	String name;

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
	public T calculateError(GraphSlamConstraint<T> constraint)
	{
		return position.minus(constraint.node.getPosition()).applyOffset(constraint.getOffset());
	}

	@Override
	public T getPosition()
	{
		return position;
	}

	@Override
	public void addConstraint(GraphSlamNode<T> node, T offset, double certainty, ConstraintOrigin constraintDirection)
	{
		GraphSlamConstraint<T> target = constraints.get(node);
		if (target != null)
		{
			if (target.constraintOrigin != constraintDirection)
			{
				throw new RuntimeException("Constraint Directions don't match");
			}

			target.addValue(offset, certainty);
		} else
		{
			constraints.put(node, new GraphSlamConstraint<>(this, node, offset, certainty, constraintDirection, zero));
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
	public void adjustPosition(T error)
	{
		position = position.minus(error);
	}

	@Override
	public void setCurrentError(T error)
	{
		currentError = error;
	}

	@Override
	public T getCurrentError()
	{
		return currentError;
	}

	@Override
	public T getNormalisedOffset(T offset)
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
	public void deleteConstraint(GraphSlamNode<T> node)
	{
		constraints.remove(node);
	}

}