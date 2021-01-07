package au.com.rsutton.navigation.router.rrt;

import java.awt.Color;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

public class RRT<T extends Pose<T>>
{
	Array2d<Integer> map;
	private RrtNode<T> root;
	private List<RrtNode<T>> nodes = new LinkedList<>();
	Array2d<List<RrtNode<T>>> nodeMap;
	private T target;
	private RrtNode<T> targetNode;
	private NodeListener<T> nodeListener;
	private RrtNode<T> solution = null;
	private int steps = 0;
	private double bestSolution = Double.MAX_VALUE;
	private double bestPath = Double.MAX_VALUE;

	static Random rand;

	RRT(T start, T target, Array2d<Integer> map, NodeListener<T> nodeListener)
	{
		this.map = map;
		this.target = target;
		this.root = new RrtNode<>(start, null, 0);
		this.targetNode = new RrtNode<>(target, null, 0);
		this.nodeListener = nodeListener;

		nodeMap = new Array2d<>(map.getMaxX(), map.getMaxY(), null);
		nodes = new LinkedList<>();
		nodes.add(root);

		isVacant(root);
		isVacant(target);

	}

	public void solve(int maxSteps)
	{
		Stopwatch timer = Stopwatch.createStarted();

		boolean found = false;

		while (steps < maxSteps && timer.elapsed(TimeUnit.SECONDS) < 100)
		{

			tryStep();
			if (solution != null && !found)
			{
				found = true;
				maxSteps = steps + 25000;
			}

		}

		if (solution != null)
		{
			writeSolution(solution);
			solution.calculateCost(target);
			notifySolution(solution, Color.RED, targetNode, nodeListener);
		}

		dumpMap();

		System.out.println("Steps " + steps);
		System.out.println("Duration " + timer.elapsed(TimeUnit.SECONDS));

		System.out.println("Solution cost is " + getPathCost(solution, nodes.size() + 10));

	}

	public int tryStep()
	{
		// pick random point in map space
		T pose = root.getRandomPointInMapSpace(map, steps);

		if (Math.random() > 0.99)
		{
			pose = target.copy();
		}

		// pick existing node with best (probabilistic) cost
		RrtNode<T> parentNode = chooseBestParentNode(pose);

		if (parentNode != null)
		{
			T newPose = parentNode.moveTowards(pose);
			if (isVacant(newPose))
			{
				steps++;
				RrtNode<T> newNode = new RrtNode<>(newPose, parentNode, parentNode.calculateCost(newPose));
				// if (newPose.canConnect(target))
				if (newPose.canConnect(target))
				{
					double cost = (newNode.calculateCost(target));// +
																	// (getPathCost(newNode)
																	// *
																	// 0.1);

					double pathCost = getPathCost(newNode, nodes.size() + 10);

					if (cost < 1 && pathCost < bestPath)
					{

						System.out.println("solution " + solution);
						System.out.println("target " + targetNode);

						solution = newNode;
						bestSolution = cost;
						bestPath = pathCost;
						System.out.println("Solution improved " + cost);
						System.out.println("New path cost " + pathCost);
						notifySolution(solution, Color.RED, targetNode, nodeListener);
					}

				}
				if (bestSolution <= 2.0)
				{
					// TODO: relinkNodes is still broken
					// relinkNodes(newNode);
				}
				nodes.add(newNode);
				nodeListener.added(newNode, Color.WHITE, false);
				if (nodeMap.get((int) newNode.getX(), (int) newNode.getY()) == null)
				{
					nodeMap.set((int) newNode.getX(), (int) newNode.getY(), new LinkedList<>());
				}
				nodeMap.get((int) newNode.getX(), (int) newNode.getY()).add(newNode);
				if (steps % 500 == 0)
				{
					System.out.println(steps);
				}

			}

		}

		return steps;
	}

	static public <T extends Pose<T>> void notifySolution(RrtNode<T> solution, Color color, RrtNode<T> targetNode,
			NodeListener<T> nodeListener)
	{
		System.out.println("solution " + solution);
		System.out.println("target " + targetNode);
		solution.canConnect(targetNode);
		System.out.println(targetNode);
		Set<RrtNode<T>> seen = new HashSet<>();
		RrtNode<T> node = solution;
		while (node != null)
		{
			if (node == node.getParent())
			{
				throw new RuntimeException("Bad parent");
			}
			if (seen.contains(node))
			{
				System.out.println("Already seen this node");
				break;
			}
			if (node.getParent() != null)
			{
				nodeListener.added(node, color, true);
			}
			seen.add(node);
			node = node.getParent();
		}

	}

	private boolean isVacant(T target2)
	{
		return map.get((int) target2.getX(), (int) target2.getY()) == 0;

	}

	private void relinkNodes(RrtNode<T> parent)
	{

		// this is still broken, it violates constraints!!!!!

		// find near nodes with score that would benifit if newNode was their
		// parent
		List<RrtNode<T>> nodesToRelink = findNodesToRelink(parent);

		for (RrtNode<T> node : nodesToRelink)
		{
			RrtNode<T> copyOfParent = node.getParent().copy();
			if (parent.canConnect(node))
			{
				node.setParent(parent, node.calculateCost(parent));
				List<RrtNode<T>> children = new LinkedList<>(node.getChildren().values());
				for (RrtNode<T> childNode : children)
				{
					if (!node.canConnect(childNode))
					{
						childNode.setParent(copyOfParent, childNode.calculateCost(copyOfParent));
					}
				}
			}
		}

	}

	static public double getPathCost(RrtNode<?> node, int limit)
	{
		RrtNode<?> tmp = node;
		// find path to root
		double cost = 0;
		int ctr = 0;
		while (tmp != null && ctr < limit)
		{
			cost += tmp.getCost();
			tmp = tmp.getParent();
			ctr++;
		}
		if (ctr == limit)
		{
			System.out.println("Path cost failure");
			cost += 1_000_000;
		}

		return cost;
	}

	private List<RrtNode<T>> findNodesToRelink(RrtNode<T> newParent)
	{

		if (nodes.isEmpty())
		{
			return Collections.emptyList();
		}

		List<RrtNode<T>> shortList = getNearbyNodes((int) newParent.getX(), (int) newParent.getY(), 20);

		List<RrtNode<T>> candidates = new LinkedList<>();
		for (RrtNode<T> node : shortList)
		{

			double oldCost = getPathCost(node.getParent(), nodes.size() + 10);
			double newCost = getPathCost(newParent, nodes.size() + 10);

			double oldDistance = node.calculateCost(node.getParent());
			double newDistance = node.calculateCost(newParent);

			if (newDistance <= 1 && newDistance + newCost < oldDistance + oldCost)
			{
				candidates.add(node);
			}

		}
		return candidates;
	}

	// private List<RrtNode<T>> getNearbyNodes(RrtNode<T> newParent)
	// {
	//
	// List<RrtNode<T>> shortList = new LinkedList<>();
	//
	// for (int x = -1; x <= 1; x++)
	// {
	// for (int y = -1; y <= 1; y++)
	// {
	// int xp = (int) (x + newParent.getX());
	// int yp = (int) (y + newParent.getY());
	// addNodeToList(shortList, xp, yp);
	// }
	// }
	// return shortList;
	// }
	//
	// private List<RrtNode<T>> getNearbyNodes(T newParent)
	// {
	//
	// List<RrtNode<T>> shortList = new LinkedList<>();
	//
	// for (int x = -1; x <= 1; x++)
	// {
	// for (int y = -1; y <= 1; y++)
	// {
	// int xp = (int) (x + newParent.getX());
	// int yp = (int) (y + newParent.getY());
	// addNodeToList(shortList, xp, yp);
	// }
	// }
	// return shortList;
	// }

	public List<RrtNode<T>> getNearbyNodes(int x, int y, int minReturn)
	{

		int maxRadius = Math.max(map.getMaxX(), map.getMaxY());

		List<RrtNode<T>> shortList = new LinkedList<>();

		addNodeToList(shortList, x, y);
		for (int r = 1; r < maxRadius; r++)
		{

			for (int p = -r; p <= r; p++)
			{
				addNodeToList(shortList, p + x, -r + y);
				addNodeToList(shortList, p + x, +r + y);
				addNodeToList(shortList, -r + x, p + y);
				addNodeToList(shortList, +r + x, p + y);

			}

			if (shortList.size() > minReturn)
			{
				break;
			}
		}

		return shortList;
	}

	private void addNodeToList(List<RrtNode<T>> shortList, int xp, int yp)
	{
		if (xp >= 0 && yp >= 0 && xp < map.getMaxX() && yp < map.getMaxY())
		{
			if (nodeMap.get(xp, yp) != null)
			{
				shortList.addAll(nodeMap.get(xp, yp));
			}
		}
	}

	private boolean isVacant(RrtNode<T> root2)
	{
		return map.get((int) root2.getX(), (int) root2.getY()) == 0;
	}

	private void writeSolution(RrtNode<T> solution)
	{
		System.out.println("solution " + solution);
		System.out.println("target " + targetNode);
		solution.canConnect(targetNode);
		System.out.println(targetNode);
		Set<RrtNode<T>> seen = new HashSet<>();
		RrtNode<T> node = solution;
		while (node != null)
		{
			int x = (int) node.getX();
			int y = (int) node.getY();
			map.set(x, y, 2);
			if (node == node.getParent())
			{
				throw new RuntimeException("Bad parent");
			}
			if (seen.contains(node))
			{
				System.out.println("Already seen this node");
				break;
			}
			System.out.println(node);
			seen.add(node);
			node = node.getParent();
		}

	}

	void dumpMap()
	{
		for (int x = 0; x < map.getMaxX(); x++)
		{
			System.out.print("*");
		}
		System.out.println("**");

		for (int y = 0; y < map.getMaxY(); y++)
		{
			System.out.print("*");

			for (int x = 0; x < map.getMaxX(); x++)
			{

				if (x == root.getX() && y == root.getY())
				{
					System.out.print("S");
				} else if (x == (int) target.getX() && y == (int) target.getY())
				{
					System.out.print("T");
				} else if (map.get(x, y) == 1)
				{
					System.out.print("*");
				} else if (map.get(x, y) == 2)
				{
					System.out.print(".");
				} else
				{
					System.out.print(" ");
				}
			}
			System.out.println("*");
		}
		for (int x = 0; x < map.getMaxX(); x++)
		{
			System.out.print("*");
		}
		System.out.println("**");
	}

	private RrtNode<T> chooseBestParentNode(T pose)
	{

		if (Math.random() > 1.75)
		{
			// choose random node

			LinkedList<RrtNode<T>> tmp = new LinkedList<>();
			tmp.addAll(nodes);

			while (!tmp.isEmpty())
			{
				RrtNode<T> node = tmp.remove((int) (Math.random() * tmp.size()));
				if (node.canConnect(pose))
				{
					return node;
				}
			}

			return null;

		}
		double bestDistance = Double.MAX_VALUE;
		RrtNode<T> selectedNode = null;

		List<RrtNode<T>> nearbyNodes = getNearbyNodes((int) pose.getX(), (int) pose.getY(), 30);
		for (RrtNode<T> node : nearbyNodes)
		{

			double distance = node.calculateCost(pose);
			if (distance < bestDistance)
			{
				if (node.canConnect(pose))
				{
					bestDistance = distance;
					selectedNode = node;

				}
			}
		}
		if (selectedNode != null || nearbyNodes.size() > 30)
		{
			return selectedNode;
		}

		for (RrtNode<T> node : nodes)
		{
			double distance = node.calculateCost(pose);
			if (distance < bestDistance)
			{
				if (node.canConnect(pose))
				{

					bestDistance = distance;
					selectedNode = node;
				}
			}
		}
		return selectedNode;
	}

	public boolean hasSolution()
	{
		return solution != null;
	}

	public RrtNode<T> getSolution()
	{
		return solution;
	}

}
