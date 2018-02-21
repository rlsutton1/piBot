package au.com.rsutton.navigation.graphslam.v3;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.rsutton.robot.rover.LogLevelHelper;

public class GraphSlamV3<T extends GraphSlamNode>
{

	Logger logger = LogManager.getLogger();

	List<T> nodes = new LinkedList<>();

	private GraphSlamNodeConstructor<T> ctor;

	private final T root;

	public GraphSlamV3(GraphSlamNodeConstructor<T> ctor)
	{
		LogLevelHelper.setLevel(logger, Level.ERROR);
		this.ctor = ctor;
		root = this.ctor.construct("init", 0);
		addNode(0, 1, root);
	}

	T getRoot()
	{
		return root;
	}

	public T addNode(double offset, double certainty, T referenceNode)
	{

		return addNode("", offset, certainty, referenceNode);
	}

	public T addNode(String name, double offset, double certainty, T referenceNode)
	{

		T node = ctor.construct(name, offset + referenceNode.getPosition());

		addConstraint(offset, node, certainty, referenceNode);

		if (nodes.isEmpty())
		{
			node.setIsRoot(true);
		}

		nodes.add(node);

		return node;
	}

	public void addConstraint(double offset, T node, double certainty, T referenceNode)
	{

		node.addConstraint(referenceNode, -offset, certainty, ConstraintOrigin.ROOT);
		referenceNode.addConstraint(node, offset, certainty, ConstraintOrigin.FLOATING);
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
		if (ctr > 4)
		{
			logger.info("Rounds " + ctr);
		}
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
				// logger.info(node);

			}
		}
		// logger.info("Total Error " + totalError);
		return totalError;
	}

	public void dump()
	{
		for (T node : nodes)
		{
			logger.error(node);
			for (GraphSlamConstraint constraint : node.getConstraints())
			{
				logger.error("-->" + constraint);
			}

		}
	}

}
