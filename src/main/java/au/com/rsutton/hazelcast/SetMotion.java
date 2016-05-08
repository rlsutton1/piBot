package au.com.rsutton.hazelcast;

import au.com.rsutton.entryPoint.units.Speed;

import com.google.common.base.Objects;

public class SetMotion extends MessageBase<SetMotion>
{

	private static final long serialVersionUID = 4595314712852835336L;
	private Speed speed;
	private Double heading;
	private long timeStamp;
	private boolean freeze = false;

	public SetMotion()
	{
		super(HcTopic.SET_MOTION);
	}

	public void setSpeed(Speed speed)
	{
		this.speed = speed;
	}

	public void setHeading(Double heading)
	{
		this.heading = heading;
	}
	
	/**
	 * disable motors completely
	 * @param freeze
	 */
	public void setFreeze(boolean freeze)
	{
		this.freeze = freeze;
	}

	public Speed getSpeed()
	{
		return speed;
	}

	public Double getHeading()
	{
		return heading;
	}

	public long getTimeStamp()
	{
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp)
	{
		this.timeStamp = timeStamp;
	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper(SetMotion.class).add("Speed", speed)
				.add("Heading", heading).toString();
	}

	public boolean getFreeze()
	{
		return freeze;
	}
}
