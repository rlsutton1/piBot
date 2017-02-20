package au.com.rsutton.navigation.graphslam;

public class DimensionXYTheta implements Dimension
{

	double x;
	double y;

	// store theta as a vector pair
	double thetaX;
	double thetaY;

	public DimensionXYTheta()
	{

	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param thetaRadians
	 */
	DimensionXYTheta(double x, double y, double thetaRadians)
	{
		this.x = x;
		this.y = y;
		thetaX = Math.cos(thetaRadians);
		thetaY = Math.sin(thetaRadians);
	}

	@Override
	public int getDimensions()
	{
		return 4;
	}

	double getX()
	{
		return x;
	}

	public double getY()
	{
		return y;
	}

	/**
	 * 
	 * @return theta in radians
	 */
	public double getTheta()
	{
		return Math.atan2(thetaY, thetaX);
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
			return thetaX;
		}
		if (i == 3)
		{
			return thetaY;
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
			thetaX = value;
		} else if (i == 3)
		{
			thetaY = value;
		} else
		{
			throw new RuntimeException(i + " is not a valid dimension");
		}
	}

	@Override
	public String toString()
	{
		return "DimensionXYZ [x=" + x + ", y=" + y + ", theta=" + getTheta() + "(radians)]";
	}

}
