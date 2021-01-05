package au.com.rsutton.navigation.router.rrt;

import java.util.concurrent.atomic.AtomicInteger;

class RrtNode<T extends Pose<T>>
{
	static final AtomicInteger seed = new AtomicInteger();

	int id = seed.getAndIncrement();
	private T pose;
	private RrtNode<T> parent;
	private double cost;

	public RrtNode<T> getParent()
	{
		return parent;
	}

	public double getCost()
	{
		return cost;
	}

	public void setParent(RrtNode<T> parent, double cost)
	{
		if (parent == this)
		{
			throw new RuntimeException("Illegal parent");
		}
		this.parent = parent;
		this.cost = cost;
		this.pose.updateParent(parent.pose);
	}

	RrtNode(T pose, RrtNode<T> parent, double cost)
	{
		this.pose = pose;
		if (parent != null)
		{
			this.parent = parent;

		} else
		{
			System.out.println("Parent is null");
		}
		this.cost = cost;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		@SuppressWarnings("unchecked")
		RrtNode<T> other = (RrtNode<T>) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public void setCost(double calculateCost)
	{
		this.cost = calculateCost;

	}

	public T getRandomPointInMapSpace(Array2d<Integer> map, int steps)
	{
		return pose.getRandomPointInMapSpace(map, steps);
	}

	public T moveTowards(T pose2)
	{
		return pose.moveTowards(pose2);
	}

	public double getX()
	{
		return pose.getX();
	}

	public double getY()
	{
		return pose.getY();
	}

	public double calculateCost(RrtNode<T> from)
	{
		return pose.calculateCost(from.pose);
	}

	public double calculateCost(T from)
	{
		return pose.calculateCost(from);
	}

	public boolean canConnect(T pose2)
	{
		return pose.canConnect(pose2);
	}

	public boolean canConnect(RrtNode<T> pose2)
	{
		return pose.canConnect(pose2.pose);
	}

	public T getPose()
	{
		return pose;
	}

	@Override
	public String toString()
	{
		return "RrtNode [id=" + id + ", cost=" + cost + ", pose=" + pose + "]";
	}

}