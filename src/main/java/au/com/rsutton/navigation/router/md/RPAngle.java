package au.com.rsutton.navigation.router.md;

public class RPAngle
{
	private final int degrees;

	public RPAngle(RPAngle angle)
	{
		this(angle.degrees);
	}

	public RPAngle(int degrees)
	{
		int tmp = degrees % 360;
		if (tmp < 0)
		{
			tmp += 360;
		}

		this.degrees = tmp;

	}

	public double getRadians()
	{
		return Math.toRadians(degrees);
	}

	public RPAngle invert()
	{
		return new RPAngle(getDegrees() - 180);
	}

	public int getDegrees()
	{
		return degrees;
	}

	@Override
	public String toString()
	{
		return "Angle [angle=" + degrees + "]";
	}
}
