package au.com.rsutton.navigation.graphslam;

public class DimensionXYZ implements Dimension
{

	double x;
	double y;
	double z;

	DimensionXYZ(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public int getDimensions()
	{
		return 3;
	}

	double getX()
	{
		return x;
	}

	public double getY()
	{
		return y;
	}

	public double getZ()
	{
		return z;
	}

	@Override
	public double get(int i)
	{
		if (i == 0)
		{
			return x;
		}
		if (i == 1)
		{
			return y;
		}
		if (i == 2)
		{
			return z;
		}
		throw new RuntimeException(i + " is not a valid dimension");
	}

	@Override
	public void set(int i, double value)
	{
		if (i == 0)
		{
			x = value;
		} else if (i == 1)
		{
			y = value;
		} else if (i == 2)
		{
			z = value;
		} else
		{
			throw new RuntimeException(i + " is not a valid dimension");
		}
	}

	@Override
	public String toString()
	{
		return "DimensionXYZ [x=" + x + ", y=" + y + ", z=" + z + "]";
	}

}
