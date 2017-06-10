package au.com.rsutton.navigation.graphslam.v3;

class GraphSlamConstraint
{
	final GraphSlamNode node;
	final GraphSlamNode parentNode;
	private final GraphSlamWeightedAverage offset = new GraphSlamWeightedAverage();

	final ConstraintOrigin constraintOrigin;

	GraphSlamConstraint(GraphSlamNode parentNode, GraphSlamNode node, double offset, double certainty,
			ConstraintOrigin constraintDirection)
	{
		this.parentNode = parentNode;
		this.node = node;
		this.offset.addValue(offset, certainty);
		this.constraintOrigin = constraintDirection;
	}

	@Override
	public String toString()
	{
		return "Constraint [node=" + node + ", offset=" + offset + " origin=" + constraintOrigin + "]";
	}

	public double getOffset()
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
		GraphSlamConstraint other = (GraphSlamConstraint) obj;
		if (node == null)
		{
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		return true;
	}

	public void addValue(double offset2, double certainty)
	{
		offset.addValue(offset2, certainty);

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
