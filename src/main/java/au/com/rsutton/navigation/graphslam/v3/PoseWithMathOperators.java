package au.com.rsutton.navigation.graphslam.v3;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.angle.AngleUtil;

public class PoseWithMathOperators implements MathOperators<PoseWithMathOperators>
{

	final double x;
	final double y;
	final double angle;

	PoseWithMathOperators(double x, double y, double angle)
	{
		this.x = x;
		this.y = y;
		this.angle = angle;
	}

	@Override
	public PoseWithMathOperators applyOffset(PoseWithMathOperators offset)
	{
		// the rotation of this pose will rotate the incoming value
		Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(angle));

		Vector3D vector2 = rotation.applyTo(new Vector3D(offset.x, offset.y, 0d));

		// Vector3D vector2 = new Vector3D(value.x, value.y, 0d);

		return new PoseWithMathOperators(vector2.getX() + this.x, vector2.getY() + this.y,
				AngleUtil.normalize(this.angle + offset.angle));
	}

	@Override
	public PoseWithMathOperators minus(PoseWithMathOperators value)
	{

		return new PoseWithMathOperators(this.x - value.x, this.y - value.y,
				AngleUtil.normalize(AngleUtil.normalize(this.angle) - AngleUtil.normalize(value.angle)));
	}

	@Override
	public PoseWithMathOperators adjust(PoseWithMathOperators adjustment)
	{

		return new PoseWithMathOperators(adjustment.x + this.x, adjustment.y + this.y,
				AngleUtil.normalize(this.angle + adjustment.angle));
	}

	@Override
	public PoseWithMathOperators inverse()
	{
		// angle is a delta : range -180 to +180

		double na = angle;
		if (na > 180)
		{
			na -= 360;
		}
		if (na < -180)
		{
			na += 360;
		}

		return new PoseWithMathOperators(this.x * -1, this.y * -1, na * -1);
	}

	@Override
	public PoseWithMathOperators zero()
	{
		return new PoseWithMathOperators(0, 0, 0);
	}

	@Override
	public String toString()
	{
		return "" + round(x) + "," + round(y) + "," + angle;
	}

	private double round(double v)
	{
		return ((int) (v * 100)) / 100.0;
	}

	List<PoseWithMathOperators> valuesForAverage = new LinkedList<>();
	double totalWeight = 0;

	@Override
	public void addWeightedValueForAverage(PoseWithMathOperators value, double weight)
	{
		valuesForAverage.add(value);
		totalWeight += weight;

	}

	@Override
	public PoseWithMathOperators getWeightedAverage()
	{
		double xt = 0;
		double yt = 0;

		List<Double> angles = new LinkedList<>();

		for (PoseWithMathOperators pose : valuesForAverage)
		{
			xt += pose.x;
			yt += pose.y;
			angles.add(pose.angle);
		}

		double averageAngle = AngleUtil.getAverageAngle(angles);

		return new PoseWithMathOperators(xt / valuesForAverage.size(), yt / valuesForAverage.size(), averageAngle);
	}

	@Override
	public PoseWithMathOperators copy()
	{
		return new PoseWithMathOperators(x, y, angle);
	}

	@Override
	public double getWeight()
	{
		return totalWeight;
	}

}
