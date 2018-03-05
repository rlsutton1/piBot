package au.com.rsutton.navigation.graphslam.v3;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.rsutton.robot.rover.LogLevelHelper;

public class GraphSlamV3<N extends GraphSlamNode<V>, V extends MathOperators<V>>
{

	Logger logger = LogManager.getLogger();

	List<N> nodes = new LinkedList<>();

	private GraphSlamNodeConstructor<N, V> ctor;

	private final N root;

	public GraphSlamV3(GraphSlamNodeConstructor<N, V> ctor)
	{
		LogLevelHelper.setLevel(logger, Level.ERROR);
		this.ctor = ctor;
		root = this.ctor.construct("init", ctor.zero());
		addNode(ctor.zero(), 1, root);
	}

	N getRoot()
	{
		return root;
	}

	public N addNode(V offset, double certainty, N referenceNode)
	{

		return addNode("", offset, certainty, referenceNode);
	}

	public N addNode(String name, V offset, double certainty, N referenceNode)
	{

		N node = ctor.construct(name, offset);

		addConstraint(offset, node, certainty, referenceNode);

		if (nodes.isEmpty())
		{
			node.setIsRoot(true);
		}

		nodes.add(node);

		return node;
	}

	public void addConstraint(V offset, N node, double certainty, N referenceNode)
	{

		node.addConstraint(referenceNode, offset.inverse(), certainty, ConstraintOrigin.FLOATING);
		referenceNode.addConstraint(node, offset, certainty, ConstraintOrigin.FLOATING);
	}

	public void solve()
	{
		int ctr = 0;
		while (ctr < 10)
		{
			ctr++;
			V error = updatePositions();

		}
		if (ctr > 4)
		{
			logger.info("Rounds " + ctr);
		}
	}

	private V updatePositions()
	{
		V totalError = ctor.zero();
		for (N node : nodes)
		{
			V error = ctor.zero();
			for (GraphSlamConstraint<V> constraint : node.getConstraints())
			{
				V err = node.calculateError(constraint);
				// if (!node.isRoot())
				// logger.error(node + " err=" + err);
				double weight = node.getWeight();
				error.addWeightedValueForAverage(err, weight);

			}

			node.setCurrentError(error.getWeightedAverage());
			totalError = totalError.adjust(error.getWeightedAverage());
		}
		for (N node : nodes)
		{
			if (!node.isRoot())
			{
				node.adjustPosition(node.getCurrentError());
				// logger.info(node);

			}
		}
		// logger.info("Total Error " + totalError);
		return totalError;
	}

	public void dump()
	{
		System.out.println("Dumping Graph Slam V3");
		for (N node : nodes)
		{
			logger.error(node);
			for (GraphSlamConstraint<V> constraint : node.getConstraints())
			{
				logger.error("-->" + constraint);
			}

		}
	}

}
