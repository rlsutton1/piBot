package au.com.rsutton.navigation.router.nextgen;

import au.com.rsutton.units.Angle;
import au.com.rsutton.units.Speed;
import au.com.rsutton.units.Time;

public class ControlAction
{
	final Speed speed;
	final Angle angle;
	final Time duration;

	ControlAction(Speed speed, Angle angle, Time duration)
	{
		this.speed = speed;
		this.angle = angle;
		this.duration = duration;
	}

	public Speed getSpeed()
	{
		return speed;
	}

	public Angle getAngle()
	{
		return angle;
	}

	public Time getDuration()
	{
		return duration;
	}

}
