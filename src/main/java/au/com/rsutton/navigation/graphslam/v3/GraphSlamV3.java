package au.com.rsutton.navigation.graphslam.v3;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.rsutton.robot.rover.LogLevelHelper;

public class GraphSlamV3<N extends GraphSlamNode<V>, V extends MathOperators<V>>
{

	private Logger logger = LogManager.getLogger();

	private List<N> nodes = new LinkedList<>();

	private GraphSlamNodeConstructor<N, V> ctor;

	private final N root;

	public GraphSlamV3(GraphSlamNodeConstructor<N, V> ctor)
	{
		LogLevelHelper.setLevel(logger, Level.ERROR);
		this.ctor = ctor;
		root = this.ctor.construct("init", ctor.zero());
		root.setIsRoot(true);
		nodes.add(root);
	}

	public N getRoot()
	{
		return root;
	}

	public N addNode(V initialPosition, V offset, double certainty, N referenceNode)
	{

		return addNode("", initialPosition, offset, certainty, referenceNode);
	}

	public N addNode(String name, V initialPosition, V offset, double certainty, N referenceNode)
	{

		N node = ctor.construct(name, initialPosition);

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

		// node.addConstraint(referenceNode, offset.inverse(), certainty);
		referenceNode.addConstraint(node, offset, certainty);
	}

	public void solve()
	{
		int ctr = 0;
		while (ctr < 1000)
		{
			ctr++;
			updatePositions();

		}
		if (ctr > 4)
		{
			logger.info("Rounds " + ctr);
		}
	}

	private V updatePositions()
	{
		for (N node : nodes)
		{
			node.clearError();
		}
		for (N node : nodes)
		{
			for (GraphSlamConstraint<V> constraint : node.getConstraints())
			{
				constraint.getNode().addCalculatedError(constraint);
			}

		}
		for (N node : nodes)
		{
			if (!node.isRoot())
			{
				node.adjustPosition();
			}
		}
		// logger.info("Total Error " + totalError);
		return null;
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
				constraint.dumpObservations();
			}

		}
	}

}
