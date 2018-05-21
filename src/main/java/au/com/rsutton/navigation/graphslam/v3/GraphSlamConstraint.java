package au.com.rsutton.navigation.graphslam.v3;

public class GraphSlamConstraint<T extends MathOperators<T>>
{
	private final GraphSlamNode<T> node;
	private final GraphSlamNode<T> parentNode;
	private final T observations;

	GraphSlamConstraint(GraphSlamNode<T> parentNode, GraphSlamNode<T> node, T offset, double certainty, T zero)
	{
		this.observations = zero.copy();
		this.parentNode = parentNode;
		this.node = node;
		this.observations.addWeightedValueForAverage(new WeightedPose<>(offset, certainty));
	}

	@Override
	public String toString()
	{
		String tmp = "Constraint [node=" + node + "]";
		observations.dumpObservations();
		return tmp;
	}

	public T getOffset()
	{
		return observations.getWeightedAverage();
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

	public void addValue(WeightedPose<T> offset2)
	{
		observations.addWeightedValueForAverage(offset2);

	}

	void dumpObservations()
	{
		observations.dumpObservations();
	}

	GraphSlamNode<T> getParentNode()
	{
		return parentNode;
	}

	GraphSlamNode<T> getNode()
	{
		return node;
	}

}
