package au.com.rsutton.navigation.graphslam.v3;

import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Preconditions;

public class GraphSlamV3<T extends GraphSlamNode>
{

	List<T> nodes = new LinkedList<>();

	T currentPosition;

	private GraphSlamNodeConstructor<T> ctor;

	public GraphSlamV3(GraphSlamNodeConstructor<T> ctor)
	{
		this.ctor = ctor;

		currentPosition = this.ctor.construct("init", 0);
	}

	public T addNode(double offset, double certainty)
	{
		return addNode("", offset, certainty);
	}

	public T addNode(String name, double offset, double certainty)
	{

		T node = ctor.construct(name, offset + currentPosition.getPosition());

		addConstraint(offset, node, certainty);

		if (nodes.isEmpty())
		{
			node.setIsRoot(true);
		}

		nodes.add(node);

		return node;
	}

	public T move(double offset, double certainty)
	{
		return move("", offset, certainty);
	}

	public T move(String name, double offset, double certainty)
	{
		T previousPosition = currentPosition;

		T newPosition = addNode(name, offset, certainty);
		currentPosition = newPosition;

		if (!previousPosition.isRoot())
		{
			// deleteNodeRetainConstraints(previousPosition);
		}

		return currentPosition;
	}

	public void addConstraint(double offset, T node, double certainty)
	{

		node.addConstraint(currentPosition, -offset, certainty, ConstraintOrigin.ROOT);
		currentPosition.addConstraint(node, offset, certainty, ConstraintOrigin.FLOATING);
	}

	public void deleteNode(T node)
	{
		Preconditions.checkArgument(!node.equals(currentPosition), "can not delete current position");
		nodes.remove(node);
		for (T existing : nodes)
		{
			existing.deleteConstraint(node);
		}
	}

	public void deleteNodeRetainConstraints(T node)
	{
		Preconditions.checkArgument(!node.equals(currentPosition), "can not delete current position");

		// add linking constraints between all nodes constrained through this
		// node we're about to delete
		for (GraphSlamConstraint constraint1 : node.getConstraints())
		{

			for (GraphSlamConstraint constraint2 : node.getConstraints())
			{
				if (!constraint1.equals(constraint2))
				{
					double offset = constraint2.getOffset() - constraint1.getOffset();
					double weight = Math.min(constraint1.getWeight(), constraint2.getWeight());
					constraint1.node.addConstraint(constraint2.node, offset, weight, constraint2.constraintOrigin);
					constraint2.node.addConstraint(constraint1.node, -offset, weight, constraint1.constraintOrigin);
				}
			}
		}
		deleteNode(node);

	}

	public void solve()
	{
		double lastError = 1;
		double errorChange = 10000;
		int ctr = 0;
		while (errorChange > 1 && ctr < 10)
		{
			ctr++;
			double error = updatePositions();
			errorChange = Math.abs(error - lastError);
			lastError = error;

		}
		System.out.println("Current Position " + currentPosition);
		System.out.println("Rounds " + ctr);

	}

	private double updatePositions()
	{
		double totalError = 0;
		for (T node : nodes)
		{
			GraphSlamWeightedAverage error = new GraphSlamWeightedAverage();
			for (GraphSlamConstraint constraint : node.getConstraints())
			{
				double err = node.calculateError(constraint);
				double weight = node.getWeight();
				error.addValue(err, weight);

			}

			node.setCurrentError(error.getWeightedAverage());
			totalError += error.getWeightedAverage();
		}
		for (T node : nodes)
		{
			if (!node.isRoot())
			{
				node.adjustPosition(node.getCurrentError());
				// System.out.println(node);

			}
		}
		// System.out.println("Total Error " + totalError);
		return totalError;
	}

	public void dump()
	{
		for (T node : nodes)
		{
			System.out.println(node);
			for (GraphSlamConstraint constraint : node.getConstraints())
			{
				System.out.println("-->" + constraint);
			}

		}
	}

}
