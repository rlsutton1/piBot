package au.com.rsutton.hazelcast;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.Speed;

import com.google.common.base.Objects;

public class RobotLocation extends MessageBase<RobotLocation>
{

	private static final long serialVersionUID = 938950572423708619L;
	private int heading;
	private Distance x;
	private Distance y;
	private Speed speed;

	public RobotLocation()
	{
		super(HcTopic.LOCATION);

	}

	public void setHeading(int outx)
	{
		this.heading = outx;

	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper(RobotLocation.class).add("x", x)
				.add("y", y).add("heading", heading).toString();
	}

	public void setX(Distance distance)
	{
		x = distance;

	}

	public void setY(Distance currentY)
	{
		y = currentY;

	}

	public Distance getX()
	{
		return x;
	}

	public Distance getY()
	{
		return y;
	}

	public int getHeading()
	{
		return heading;
	}

	public void setSpeed(Speed speed)
	{
		this.speed = speed;
		
	}
}
