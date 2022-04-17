package au.com.rsutton.units;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class DistanceXY
{
	Distance x;
	Distance y;

	public DistanceXY(double x, double y, DistanceUnit unit)
	{
		this.x = new Distance(x, unit);
		this.y = new Distance(y, unit);
	}

	public Distance getX()
	{
		return x;
	}

	public Distance getY()
	{
		return y;
	}

	public Vector3D getVector(DistanceUnit unit)
	{
		return new Vector3D(x.convert(unit), y.convert(unit), 0);
	}

	@Override
	public String toString()
	{
		return "DistanceXY [x=" + x + ", y=" + y + "]";
	}

	public DistanceXY add(DistanceXY point)
	{
		return new DistanceXY(x.convert(DistanceUnit.CM) + point.x.convert(DistanceUnit.CM),
				y.convert(DistanceUnit.CM) + point.y.convert(DistanceUnit.CM), DistanceUnit.CM);
	}

	public DistanceXY subtract(DistanceXY point)
	{
		return new DistanceXY(x.convert(DistanceUnit.CM) - point.x.convert(DistanceUnit.CM),
				y.convert(DistanceUnit.CM) - point.y.convert(DistanceUnit.CM), DistanceUnit.CM);
	}
}
