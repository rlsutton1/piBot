package au.com.rsutton.navigation.router.rrt;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;

public class RrtNode<T extends Pose<T>>
{
	static final AtomicInteger seed = new AtomicInteger();

	int id = seed.getAndIncrement();
	private T pose;
	private RrtNode<T> parent;
	private Map<Integer, RrtNode<T>> children = new HashMap<>();
	private double cost;

	public RrtNode<T> getParent()
	{
		return parent;
	}

	public double getCost()
	{
		return cost;
	}

	public Map<Integer, RrtNode<T>> getChildren()
	{
		return children;
	}

	public void discard()
	{
		children.clear();
		parent = null;
		pose = null;
	}

	public void setParent(RrtNode<T> parent, double cost)
	{
		if (parent == this)
		{
			throw new RuntimeException("Illegal parent");
		}

		if (this.parent != null)
		{
			this.parent.children.remove(id);
		}
		parent.children.put(id, this);

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
			parent.children.put(id, this);

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

	public T getRandomPointInMapSpace(ProbabilityMapIIFc map, RttPhase rttPhase)
	{
		return pose.getRandomPointInMapSpace(map, rttPhase);
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

	public boolean canBridge(RrtNode<T> pose2)
	{
		return pose.canBridge(pose2.getPose());
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

	public RrtNode<T> copy()
	{
		return new RrtNode<>(pose.copy(), parent, cost);
	}

}