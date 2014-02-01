package au.com.rsutton.entryPoint.sonar;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;

import com.google.common.base.Objects;

public class RangeRateData
{

	private Time time;
	private long angle;
	private Distance distance;
	private Speed rateOfClose;
	private double changeInRateOfClose;

	public RangeRateData(Time time, long angle, Distance distance,
			Speed rateOfClose, double changeInRateOfClose)
	{
		this.time = time;
		this.angle = angle;
		this.distance = distance;
		this.rateOfClose = rateOfClose;
		this.changeInRateOfClose = changeInRateOfClose;
	}

	@Override
	public String toString()

	{

		return Objects.toStringHelper(RangeRateData.class).add("time", time)
				.add("angle", angle).add("distance", distance)
				.add("rateOfClose", rateOfClose)
				.add("Change in rate of close", changeInRateOfClose).toString();
	}

	public long getAngle()
	{
		return angle;
	}

	public double getChangeInRateOfClose()
	{
		return changeInRateOfClose;
	}

	public Time getTime()
	{
		return time;
	}

	public Distance getDistance()
	{
		return distance;
	}

	public Speed getRateOfClose()
	{
		return rateOfClose;
	}

}
