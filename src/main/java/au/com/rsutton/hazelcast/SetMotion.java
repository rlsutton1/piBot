package au.com.rsutton.hazelcast;

import com.google.common.base.Objects;

import au.com.rsutton.units.Speed;

public class SetMotion extends MessageBase<SetMotion>
{

	private static final long serialVersionUID = 4595314712852835336L;
	private Speed speed;
	private long turnRadius;
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

	public void setTurnRadius(long turnRadius)
	{
		this.turnRadius = turnRadius;
	}

	/**
	 * disable motors completely
	 * 
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

	public long getTurnRadius()
	{
		return turnRadius;
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
		return Objects.toStringHelper(SetMotion.class).add("Speed", speed).add("turnRadius", turnRadius).toString();
	}

	public boolean getFreeze()
	{
		return freeze;
	}
}
