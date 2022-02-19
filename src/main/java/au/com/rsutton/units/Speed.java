package au.com.rsutton.units;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Objects;

public class Speed implements Serializable
{
	private static final long serialVersionUID = 2482623954296599283L;
	public static final Speed ZERO = new Speed(Distance.ZERO, Time.perSecond());
	Time time;
	Distance distance;

	public Speed(Distance distance, Time time)
	{
		this.time = time;
		this.distance = distance;
	}

	@Override
	public boolean equals(Object o)
	{
		Speed d = (Speed) o;
		return this.getSpeed(DistanceUnit.MM, TimeUnit.MILLISECONDS) == d.getSpeed(DistanceUnit.MM,
				TimeUnit.MILLISECONDS);

	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper(Speed.class).add("distance", distance).add("time", time).toString();
	}

	public static Speed cmPerSec(double value)
	{
		return new Speed(new Distance(value, DistanceUnit.CM), Time.perSecond());
	}

	public double getCmPerSec()
	{
		return getSpeed(DistanceUnit.CM, TimeUnit.SECONDS);
	}

	public double getSpeed(DistanceUnit distanceU, TimeUnit timeU)
	{

		// need all this scaling as TimeUnit will convert 300ms to 0 seconds!

		double dscale = distanceU.convert(1, DistanceUnit.MM);
		double sscale = TimeUnit.MILLISECONDS.convert(1, timeU);

		double mm = distance.convert(DistanceUnit.MM);
		double ms = time.convert(TimeUnit.MILLISECONDS);
		double speed = (mm / dscale) / (ms / sscale);

		return speed;
	}
}
