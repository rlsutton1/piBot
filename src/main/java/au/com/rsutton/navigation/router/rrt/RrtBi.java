package au.com.rsutton.navigation.router.rrt;

import java.awt.Color;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;

public class RrtBi<T extends Pose<T>>
{

	private RrtNode<T> solution1 = null;
	private RrtNode<T> solution2 = null;
	private double bestPath = Double.MAX_VALUE;

	RRT<T> rrt1;
	RRT<T> rrt2;
	private NodeListener<T> nodeListener;
	private RrtNode<T> targetNode;
	boolean found = false;
	private ProbabilityMapIIFc map;

	static Random rand;

	public RrtBi(T start, T target, ProbabilityMapIIFc map, NodeListener<T> nodeListener)
	{
		this.map = map;
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
				List<List<RrtNode<T>>> nearNodes;
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

				for (List<RrtNode<T>> list : nearNodes)
				{
					for (RrtNode<T> nearNode : list)
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
									RRT.notifySolution(null, solution1, Color.RED, targetNode, nodeListener);
									RRT.notifySolution(null, solution2, Color.RED, targetNode, nodeListener);
								}
								found = true;
							}

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

		solution1 = null;
		solution2 = null;
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

		if (solution1 != null)
		{
			RRT.notifySolution(null, solution1, Color.RED, targetNode, nodeListener);
		}

		if (solution2 != null)
		{
			RRT.notifySolution(null, solution2, Color.RED, targetNode, nodeListener);
		}

		if (solution1 != null)
		{
			return solution1;
		}

		if (solution2 != null)
		{
			return solution2;
		}

		return rrt1.getSolution();

	}

}
