package au.com.rsutton.hazelcast;

import au.com.rsutton.units.Angle;
import au.com.rsutton.units.Distance;

public class RobotTelemetry extends MessageBase<RobotTelemetry>
{

	private static final long serialVersionUID = 938950572423708619L;
	private Angle deadReaconingHeading;

	private Distance distanceTravelled;

	private long time = System.currentTimeMillis();
	private boolean bumpLeft;
	private boolean bumpRight;

	public RobotTelemetry()
	{
		super(HcTopic.TELEMETRY);

	}

	public void setTopic()
	{

		this.topicInstance = HazelCastInstance.getInstance().getTopic(HcTopic.TELEMETRY.toString());
	}

	public void setDeadReaconingHeading(Angle angle)
	{
		this.deadReaconingHeading = angle;

	}

	public Angle getDeadReaconingHeading()
	{
		return deadReaconingHeading;
	}

	public long getTime()
	{
		return time;
	}

	public Distance getDistanceTravelled()
	{
		return distanceTravelled;
	}

	public void setDistanceTravelled(Distance distanceTravelled)
	{
		this.distanceTravelled = distanceTravelled;
	}

	public void setTime(long time)
	{
		this.time = time;
	}

	@Override
	public String toString()
	{
		return "RobotLocation [deadReaconingHeading=" + deadReaconingHeading + ", distanceTravelled="
				+ distanceTravelled + "]";
	}

	public void setBumpLeft(boolean bumpLeft)
	{
		this.bumpLeft = bumpLeft;

	}

	public void setBumpRight(boolean bumpRight)
	{
		this.bumpRight = bumpRight;
	}

	public boolean isBumpLeft()
	{
		return bumpLeft;
	}

	public boolean isBumpRight()
	{
		return bumpRight;
	}

}
