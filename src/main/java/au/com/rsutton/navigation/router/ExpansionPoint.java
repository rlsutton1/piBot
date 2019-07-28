package au.com.rsutton.navigation.router;

import java.util.LinkedList;
import java.util.List;

public final class ExpansionPoint implements Comparable<ExpansionPoint>
{
	private Double totalCost;
	private ExpansionPoint parent;

	private Double pathAngle;
	private Double pathAngularVelocity = 0.0;

	public ExpansionPoint(int x2, int y2, double totalCost, ExpansionPoint parent)
	{
		x = x2;
		y = y2;
		this.totalCost = totalCost;
		this.parent = parent;

	}

	public ExpansionPoint getParent()
	{
		return parent;
	}

	@Override
	public String toString()
	{
		return x + "," + y;
	}

	final int x;
	final int y;

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public Double getTotalCost()
	{
		return totalCost;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
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
		ExpansionPoint other = (ExpansionPoint) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	@Override
	public int compareTo(ExpansionPoint o)
	{
		return totalCost.compareTo(o.totalCost);
	}

	public Double getPathAngularVelocity()
	{
		return pathAngularVelocity;
	}

	public Double getPathAngle()
	{
		return pathAngle;
	}

	public void setPathAngle(double d)
	{
		pathAngle = d;
	}

	public void setAngularVelocity(double newAngularVelocity)
	{
		pathAngularVelocity = newAngularVelocity;
	}

	public int distanceTo(int pfX, int pfY)
	{
		// pythagoras
		return (int) Math.sqrt(Math.pow(pfX - x, 2) + Math.pow(pfY - y, 2));
	}

	public void setTotalCost(Double totalCost)
	{
		this.totalCost = totalCost;
	}

	public List<ExpansionPoint> getNeighbours(int blockSize)
	{
		List<ExpansionPoint> points = new LinkedList<>();

		points.add(new ExpansionPoint(x + blockSize, y + 0, 0, this));
		points.add(new ExpansionPoint(x - blockSize, y + 0, 0, this));
		points.add(new ExpansionPoint(x + blockSize, y + blockSize, 0, this));
		points.add(new ExpansionPoint(x - blockSize, y + blockSize, 0, this));
		points.add(new ExpansionPoint(x + blockSize, y - blockSize, 0, this));
		points.add(new ExpansionPoint(x - blockSize, y - blockSize, 0, this));
		points.add(new ExpansionPoint(x + 0, y + blockSize, 0, this));
		points.add(new ExpansionPoint(x + 0, y - blockSize, 0, this));

		return points;
	}
}