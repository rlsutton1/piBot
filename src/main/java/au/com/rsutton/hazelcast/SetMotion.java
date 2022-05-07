package au.com.rsutton.hazelcast;

import com.google.common.base.Objects;

import au.com.rsutton.units.Angle;
import au.com.rsutton.units.Speed;

public class SetMotion extends MessageBase<SetMotion>
{

	private static final long serialVersionUID = 4595314712852835336L;
	private Speed speed;
	private long timeStamp;
	private boolean freeze = false;
	private Angle steeringAngle;

	public SetMotion()
	{
		super(HcTopic.SET_MOTION);
	}

	public void setSpeed(Speed speed)
	{
		this.speed = speed;
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

	public void setSteeringAngle(Angle steeringAngle)
	{
		this.steeringAngle = steeringAngle;

	}

	public Angle getSteeringAngle()
	{
		return steeringAngle;
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
		return Objects.toStringHelper(SetMotion.class).add("Speed", speed).add("steeringAngle", steeringAngle)
				.toString();
	}

	public boolean getFreeze()
	{
		return freeze;
	}

}
