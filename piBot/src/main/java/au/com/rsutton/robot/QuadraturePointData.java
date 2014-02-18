package au.com.rsutton.robot;

public class QuadraturePointData
{

	private double left;
	private double right;
	private double heading;

	public QuadraturePointData(double left,
			double right, double heading)
	{
		this.left = left;
		this.right = right;
		this.heading = heading;
	}

	public double getLeft()
	{
		return left;
	}

	public double getRight()
	{
		return right;
	}

	public double getHeading()
	{
		return heading;
	}
}
