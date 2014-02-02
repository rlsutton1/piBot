package au.com.rsutton.entryPoint.units;

import java.io.Serializable;

import com.google.common.base.Objects;

public class Distance implements Serializable
{

	private static final long serialVersionUID = -3878188969665027416L;
	final private double value;
	final private DistanceUnit units = DistanceUnit.MM;

	public Distance(double value, DistanceUnit units)
	{
		this.value =  units.convert(value, DistanceUnit.MM);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(value);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Distance other = (Distance) obj;
		if (Double.doubleToLongBits(value) != Double
				.doubleToLongBits(other.value))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper(Distance.class).add("value", value)
				.add("units", units).toString();
	}

	public double convert(DistanceUnit unit)
	{
		double ret = value;
		return units.convert(ret, unit);

	}
}
