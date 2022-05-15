package au.com.rsutton.navigation.router.md;

public class RpPose
{

	final private double x;
	final private double y;
	final private RPAngle angle;

	RpPose(double x, double y, RPAngle angle)
	{
		this.x = x;
		this.y = y;
		this.angle = angle;
	}

	public double getX()
	{
		return x;
	}

	public double getY()
	{
		return y;
	}

	public RPAngle getAngle()
	{
		return angle;
	}

	@Override
	public String toString()
	{
		return "InternalPose [x=" + x + ", y=" + y + ", rotation=" + angle + "]";
	}

}
