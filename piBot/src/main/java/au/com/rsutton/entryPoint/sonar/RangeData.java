package au.com.rsutton.entryPoint.sonar;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.Time;

import com.google.common.base.Objects;

public class RangeData
{
	Time time;
	Distance distance;
	long angle;

	public RangeData(long angle, Distance distance, Time time)
	{
		this.time = time;
		this.angle = angle;
		this.distance = distance;
	}
	
	@Override
	public String toString()
	{
		return Objects.toStringHelper(RangeData.class).add("time", time).add("angle", angle).add("distance", distance).toString();
	}

	public Long getAngle()
	{
		// TODO Auto-generated method stub
		return angle;
	}

	public Time getTime()
	{

		return time;
	}

	public Distance getDistance()
	{

		return distance;
	}
}
