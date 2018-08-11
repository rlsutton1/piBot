package au.com.rsutton.navigation.router;

public final class ExpansionPoint implements Comparable<ExpansionPoint>
{
	private Double rating;

	public ExpansionPoint(int x2, int y2, double rating)
	{
		x = x2;
		y = y2;
		this.rating = rating;
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

	public Double getRating()
	{
		return rating;
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
		return rating.compareTo(o.rating);
	}
}