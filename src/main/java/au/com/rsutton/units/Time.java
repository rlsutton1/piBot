package au.com.rsutton.units;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Objects;

public class Time implements Serializable
{

	private static final long serialVersionUID = -3769008212448193411L;
	final private long value;
	final private TimeUnit units;

	public Time(long value, TimeUnit units)
	{
		this.value = value;
		this.units = units;
	}

	static public Time perSecond()
	{
		return new Time(1, TimeUnit.SECONDS);
	}

	@Override
	public boolean equals(Object o)
	{
		Time t = (Time) o;
		return this.convert(units) == t.convert(units);
	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper(Time.class).add("value", value)
				.add("units", units).toString();
	}

	public long convert(TimeUnit unit)
	{
		long ret = unit.convert(value, units);
		return ret;
	}
}
