package au.com.rsutton.navigation.router.rrt;

import java.awt.Color;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

public class RrtBi<T extends Pose<T>>
{

	private RrtNode<T> solution1 = null;
	private RrtNode<T> solution2 = null;
	private double bestSolution = Double.MAX_VALUE;
	private double bestPath = Double.MAX_VALUE;

	RRT<T> rrt1;
	RRT<T> rrt2;
	private NodeListener<T> nodeListener;
	private RrtNode<T> targetNode;
	boolean found = false;

	static Random rand;

	RrtBi(T start, T target, Array2d<Integer> map, NodeListener<T> nodeListener)
	{
		this.targetNode = new RrtNode<>(target, null, 0);

		this.nodeListener = nodeListener;

		rrt1 = new RRT<>(start, target, map, getNodeListener(true));

		T start2 = start.copy().invertDirection();
		T target2 = target.copy().invertDirection();
		rrt2 = new RRT<>(target2, start2, map, getNodeListener(false));

	}

	private NodeListener<T> getNodeListener(boolean isRrt1)
	{
		return new NodeListener<T>()
		{

			@Override
			public void added(RrtNode<T> newNode, Color color, boolean forcePaint)
			{
				nodeListener.added(newNode, color, forcePaint);

				// TODO: check for rrt1 meeting rrt2
				List<RrtNode<T>> nearNodes;
				if (isRrt1)
				{
					nearNodes = rrt2.getNearbyNodes((int) newNode.getX(), (int) newNode.getY(), 100);
				} else
				{
					nearNodes = rrt1.getNearbyNodes((int) newNode.getX(), (int) newNode.getY(), 100);
				}

				if (rrt1.hasSolution())
				{

					double cost = RRT.getPathCost(rrt1.getSolution(), 1000);
					if (cost < bestPath)
					{
						bestPath = cost;
						System.out.println("Found better path " + cost);
						solution1 = rrt1.getSolution();
						solution2 = null;
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
						solution1 = rrt2.getSolution();
						solution2 = null;
						found = true;
					}
				}

				for (RrtNode<T> nearNode : nearNodes)
				{

					if (newNode.canBridge(nearNode) && newNode.calculateCost(nearNode) < 0.75)
					{

						double cost = RRT.getPathCost(newNode, 1000) + RRT.getPathCost(nearNode, 1000);
						if (cost < bestPath)
						{
							System.out.println("Found better path " + cost);
							solution1 = newNode;
							solution2 = nearNode;
							bestPath = cost;
							if (found == false)
							{
								RRT.notifySolution(solution1, Color.RED, targetNode, nodeListener);
								RRT.notifySolution(solution2, Color.RED, targetNode, nodeListener);
							}
							found = true;
						}

					}
				}

			}
		};
	}

	public void solve(long maxTime, int optimizationTime)
	{
		Stopwatch timer = Stopwatch.createStarted();

		boolean done = false;

		while (timer.elapsed(TimeUnit.SECONDS) < maxTime)
		{

			rrt1.tryStep();
			rrt2.tryStep();
			if (!done && found)
			{
				done = true;
				maxTime = timer.elapsed(TimeUnit.SECONDS) + optimizationTime;
			}

		}

		if (solution1 != null)
		{
			RRT.notifySolution(solution1, Color.RED, targetNode, nodeListener);
		}

		if (solution2 != null)
		{
			RRT.notifySolution(solution2, Color.RED, targetNode, nodeListener);
		}

		System.out.println("Duration (MS) " + timer.elapsed(TimeUnit.MILLISECONDS));

		System.out.println("Solution cost is " + bestPath);

	}

}
