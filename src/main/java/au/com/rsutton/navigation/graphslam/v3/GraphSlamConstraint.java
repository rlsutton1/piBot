package au.com.rsutton.navigation.graphslam.v3;

class GraphSlamConstraint<T extends MathOperators<T>>
{
	final GraphSlamNode<T> node;
	final GraphSlamNode<T> parentNode;
	private final T offset;

	final ConstraintOrigin constraintOrigin;

	GraphSlamConstraint(GraphSlamNode<T> parentNode, GraphSlamNode<T> node, T offset, double certainty,
			ConstraintOrigin constraintDirection, T zero)
	{
		this.offset = zero.copy();
		this.parentNode = parentNode;
		this.node = node;
		this.offset.addWeightedValueForAverage(offset, certainty);
		this.constraintOrigin = constraintDirection;
	}

	@Override
	public String toString()
	{
		return "Constraint [node=" + node + ", offset=" + offset + " origin=" + constraintOrigin + "]";
	}

	public T getOffset()
	{
		return offset.getWeightedAverage();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((node == null) ? 0 : node.hashCode());
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
		GraphSlamConstraint<T> other = (GraphSlamConstraint<T>) obj;
		if (node == null)
		{
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		return true;
	}

	public void addValue(T offset2, double certainty)
	{
		offset.addWeightedValueForAverage(offset2, certainty);

	}

	public double getWeight()
	{
		return offset.getWeight();
	}

	public ConstraintOrigin isFromRoot()
	{
		return constraintOrigin;
	}

}
