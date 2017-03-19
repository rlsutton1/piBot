package au.com.rsutton.navigation.graphslam;

public class DimensionXY implements Dimension
{

	double x;
	double y;

	public DimensionXY()
	{

	}

	public DimensionXY(double x, double y)
	{
		this.x = x;
		this.y = y;
	}

	@Override
	public int getDimensions()
	{
		return 2;
	}

	public double getX()
	{
		return x;
	}

	public double getY()
	{
		return y;
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
		} else
		{
			throw new RuntimeException(i + " is not a valid dimension");
		}
	}

	@Override
	public String toString()
	{
		return "DimensionXYZ [x=" + x + ", y=" + y + "]";
	}

}
