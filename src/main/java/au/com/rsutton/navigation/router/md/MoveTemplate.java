package au.com.rsutton.navigation.router.md;

public class MoveTemplate
{

	final double moveCost;
	final RPAngle angleDelta;
	final String name;
	final boolean forward;

	public MoveTemplate(double cost, RPAngle angleDelta, String name, boolean forward)
	{
		this.moveCost = cost;
		this.angleDelta = angleDelta;
		this.name = name;
		this.forward = forward;
	}

	public boolean isForward()
	{
		return forward;
	}

	public RPAngle getAngleDelta()
	{
		return angleDelta;
	}

	@Override
	public String toString()
	{
		return "MoveTemplate [cost=" + moveCost + ", angleDelta=" + angleDelta + "]";
	}

}
