package au.com.rsutton.navigation.router.rrt;

import java.util.Random;

import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;

public class Pose2D implements Pose<Pose2D>
{

	final double x;
	final double y;

	Random rand = new Random(1);

	public Pose2D(double x, double y)
	{
		super();
		this.x = x;
		this.y = y;
	}

	@Override
	public Pose2D getRandomPointInMapSpace(ProbabilityMapIIFc map2, RttPhase rttPhase)
	{

		double x = (rand.nextDouble() * map2.getMaxX());
		double y = (rand.nextDouble() * map2.getMaxY());

		return new Pose2D(x, y);

	}

	@Override
	public double calculateCost(Pose2D from)
	{
		double dx = x - from.x;
		double dy = y - from.y;
		return Math.sqrt((dx * dx) + (dy * dy));
	}

	@Override
	public String toString()
	{
		return "Pose2D [x=" + x + ", y=" + y + "]";
	}

	@Override
	public double getX()
	{
		return x;
	}

	@Override
	public double getY()
	{
		return y;
	}

	@Override
	public Pose2D moveTowards(Pose2D pose)
	{
		Pose2D newPose = null;
		double dx = pose.x - x;
		double dy = pose.y - y;

		double scalar = 1.0;
		if (Math.max(Math.abs(dx), Math.abs(dy)) > 1)
		{
			scalar = Math.max(Math.abs(dx), Math.abs(dy));
		}
		if (scalar > 0.01)
		{
			double xAdd = dx / scalar;
			double yAdd = dy / scalar;

			// create new node
			newPose = new Pose2D(x + xAdd, y + yAdd);
		}
		return newPose;
	}

	@Override
	public boolean canConnect(Pose2D pose2)
	{
		return true;
	}

	@Override
	public void updateParent(Pose2D parent)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Pose2D copy()
	{
		return new Pose2D(x, y);
	}

	@Override
	public Pose2D invertDirection()
	{
		return this;
	}

	@Override
	public boolean canBridge(Pose2D pose2)
	{
		return true;
	}
}
