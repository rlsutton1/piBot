package au.com.rsutton.mapping;

public class XY
{

	int x;
	int y;

	public XY(int x2, int y2)
	{
		x = x2;
		y = y2;
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
		XY other = (XY) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}
}
