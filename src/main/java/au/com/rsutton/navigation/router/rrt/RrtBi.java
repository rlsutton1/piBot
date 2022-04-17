package au.com.rsutton.navigation.router.rrt;

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;

public class RrtBi<T extends Pose<T>>
{

	private RrtNode<T> solution = null;

	private double bestPath = Double.MAX_VALUE;

	RRT<T> rrt1;
	RRT<T> rrt2;
	private NodeListener<T> nodeListener;
	private RrtNode<T> targetNode;
	boolean found = false;
	private ProbabilityMapIIFc map;

	static Random rand;

	public RrtBi(T rrt1Start, T rrt1Target, ProbabilityMapIIFc map, NodeListener<T> nodeListener)
	{
		this.map = map;
		this.targetNode = new RrtNode<>(rrt1Target, null, 0);

		this.nodeListener = nodeListener;

		rrt1 = new RRT<>(rrt1Start, rrt1Target, map, getNodeListener(true));

		T rrt2Target = rrt1Start.copy().invertDirection();
		T rrt2Start = rrt1Target.copy().invertDirection();
		rrt2 = new RRT<>(rrt2Start, rrt2Target, map, getNodeListener(false));

	}

	private NodeListener<T> getNodeListener(boolean isRrt1)
	{
		return new NodeListener<T>()
		{
			@Override
			public void added(RrtNode<T> newNode, Color color, boolean forcePaint)
			{

				nodeListener.added(newNode, color, forcePaint);

				if (rrt1.hasSolution())
				{

					double cost = RRT.getPathCost(rrt1.getSolution(), 1000);
					if (cost < bestPath)
					{
						bestPath = cost;
						System.out.println("Found better path " + cost);
						solution = rrt1.getSolution();

						found = true;
					}
				}

				if (rrt2.hasSolution())
				{

					double cost = RRT.getPathCost(rrt2.getSolution(), 1000);
					if (cost < bestPath)
					{
						bestPath = cost;
						System.out.println("Found better path " + cost);
						solution = rrt2.getSolution();

						found = true;
					}
				}

				// check for rrt1 meeting rrt2
				// TODO: merge solutions
				List<RrtNode<T>> nearNodes;
				if (isRrt1)
				{
					nearNodes = rrt2.getNearbyNodes((int) newNode.getX(), (int) newNode.getY(), 100);
				} else
				{
					nearNodes = rrt1.getNearbyNodes((int) newNode.getX(), (int) newNode.getY(), 100);
				}

				for (RrtNode<T> nearNode : nearNodes)
				{

					if (newNode.canBridge(nearNode) && newNode.calculateCost(nearNode) < 0.75)
					{

						double cost = RRT.getPathCost(newNode, 1000) + RRT.getPathCost(nearNode, 1000);
						if (cost < bestPath)
						{
							// dfg this needs work
							mergePaths(nearNode, newNode);
							System.out.println("Found better path " + cost);
							bestPath = cost;
							// both have a solution

							solution = rrt1.getSolution();
							RRT.notifySolution(newNode, Color.RED, targetNode, nodeListener);
							RRT.notifySolution(nearNode, Color.RED, targetNode, nodeListener);
							found = true;
						}
					}
				}

			}

		};
	}

	boolean oneOnly = false;

	public RrtNode<T> solveWithAlternateStartLocation(long maxTime, int optimizationTime, T start)
	{

		rrt1 = new RRT<>(start, targetNode.getPose(), map, getNodeListener(true));

		T start2 = start.copy().invertDirection();

		rrt2.changeTarget(start2);

		solution = null;

		found = false;
		oneOnly = true;

		return solve(maxTime, optimizationTime);
	}

	public RrtNode<T> solve(long maxTime, int optimizationTime)
	{
		Stopwatch timer = Stopwatch.createStarted();

		boolean done = false;

		long timeout = maxTime * 1000;

		while (timer.elapsed(TimeUnit.MILLISECONDS) < timeout && timer.elapsed(TimeUnit.SECONDS) < maxTime)
		{

			RttPhase phase = RttPhase.START;
			long elapsed = timer.elapsed(TimeUnit.MILLISECONDS);
			if (elapsed > 1000)
			{
				phase = RttPhase.NORMAL;
			}
			if (elapsed > 2500)
			{
				phase = RttPhase.LONG;
			}
			if (found == true)
			{
				phase = RttPhase.OPTIMIZE;
			}

			rrt1.tryStep(phase);
			if (!oneOnly)
				rrt2.tryStep(phase);
			if (!done && found)
			{
				done = true;
				timeout = timer.elapsed(TimeUnit.MILLISECONDS) + (optimizationTime * 1000L);
			}

		}
		System.out.println("Duration (MS) " + timer.elapsed(TimeUnit.MILLISECONDS));

		System.out.println("Solution cost is " + bestPath);

		if (solution != null)
		{
			RRT.notifySolution(solution, Color.RED, targetNode, nodeListener);
			return solution;
		}

		throw new RuntimeException("Failed");
		// result = mergePaths(rrt1.getSolution(), rrt2.getSolution());

	}

	// TODO: test this
	RrtNode<T> mergePaths(RrtNode<T> path1, RrtNode<T> path2)
	{

		// build both paths into a list
		List<RrtNode<T>> tempPath = new LinkedList<>();
		RrtNode<T> p1 = path1;
		while (p1 != null)
		{
			tempPath.add(p1);
			p1 = p1.getParent();
		}

		RrtNode<T> p2 = path2;
		while (p2 != null)
		{
			tempPath.add(p2);
			p2 = p2.getParent();
		}

		// reconstruct a single path
		RrtNode<T> mergedPath = null;
		for (RrtNode<T> node : tempPath)
		{
			RrtNode<T> copy = node.copy();
			copy.setParent(mergedPath, 0);
			mergedPath = copy;
		}

		return mergedPath;

	}

	// TODO: test this
	RrtNode<T> reversePath(RrtNode<T> path)
	{

		// build the path into a list
		List<RrtNode<T>> tempPath = new LinkedList<>();
		RrtNode<T> p1 = path;
		while (p1 != null)
		{
			tempPath.add(p1);
			p1 = p1.getParent();
		}

		// reverse it

		Collections.reverse(tempPath);

		// reconstruct the path
		RrtNode<T> reversedPath = null;
		for (RrtNode<T> node : tempPath)
		{
			RrtNode<T> copy = node.copy();
			copy.setParent(reversedPath, 0);
			reversedPath = copy;
		}

		return reversedPath;
	}

}
