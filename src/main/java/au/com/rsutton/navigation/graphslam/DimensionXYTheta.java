package au.com.rsutton.navigation.graphslam;

public class DimensionXYTheta implements Dimension
{

	double x;
	double y;
	ComponentAngle theta = ComponentAngle.createComponentAngle(0);

	public DimensionXYTheta()
	{

	}

	public static class ComponentAngle
	{
		double cx;
		double cy;

		private ComponentAngle()
		{

		}

		public static ComponentAngle createComponentAngleDelta(double robotAngle, double featureAngle)
		{
			ComponentAngle ct = new ComponentAngle();
			ct.cx = Math.cos(Math.toRadians(featureAngle)) - Math.cos(Math.toRadians(robotAngle));
			ct.cy = Math.sin(Math.toRadians(featureAngle)) - Math.sin(Math.toRadians(robotAngle));
			return ct;
		}

		public static ComponentAngle createComponentAngle(double angle)
		{
			ComponentAngle ct = new ComponentAngle();
			ct.cx = Math.cos(Math.toRadians(angle));
			ct.cy = Math.sin(Math.toRadians(angle));
			return ct;
		}

		public double getAngle()
		{
			return Math.toDegrees(Math.atan2(cy, cx));
		}

		@Override
		public String toString()
		{
			return "ComponentAngle [cx=" + cx + ", cy=" + cy + " angle=" + getAngle() + "]";
		}
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param thetaRadians
	 */
	public DimensionXYTheta(double x, double y, ComponentAngle thetaDegrees)
	{
		this.x = x;
		this.y = y;
		theta = thetaDegrees;
		System.out.println("Create " + this.toString());
	}

	@Override
	public int getDimensions()
	{
		return 4;
	}

	public double getX()
	{
		return x;
	}

	public double getY()
	{
		return y;
	}

	public ComponentAngle getThetaDegrees()
	{
		return theta;
	}

	@Override
	public double get(int i)
	{
		System.out.println("get " + this.toString());
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
			return theta.cx;
		}
		if (i == 3)
		{
			return theta.cy;
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
			theta.cx = value;
		} else if (i == 3)
		{
			theta.cy = value;
		} else

		{
			throw new RuntimeException(i + " is not a valid dimension");
		}
	}

	@Override
	public String toString()
	{
		return "DimensionXYZ [x=" + x + ", y=" + y + ", theta=" + getThetaDegrees() + "(degrees)]";
	}

}
