package au.com.rsutton.navigation.graphslam.v3;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.logging.log4j.LogManager;

import au.com.rsutton.angle.AngleUtil;

public class PoseWithMathOperators implements MathOperators<PoseWithMathOperators>
{

	private org.apache.logging.log4j.Logger logger = LogManager.getLogger();

	private final double x;
	private final double y;
	private final double angle;

	public PoseWithMathOperators(double x, double y, double angle)
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

		Vector3D vector2 = rotation.applyTo(new Vector3D(offset.getX(), offset.getY(), 0d));

		// Vector3D vector2 = new Vector3D(value.x, value.y, 0d);

		return new PoseWithMathOperators(vector2.getX() + this.x, vector2.getY() + this.y,
				AngleUtil.normalize(this.angle + offset.getAngle()));
	}

	@Override
	public PoseWithMathOperators minus(PoseWithMathOperators value)
	{

		return new PoseWithMathOperators(this.x - value.getX(), this.y - value.getY(),
				AngleUtil.normalize(AngleUtil.normalize(this.angle) - AngleUtil.normalize(value.getAngle())));
	}

	@Override
	public PoseWithMathOperators plus(PoseWithMathOperators adjustment)
	{

		return new PoseWithMathOperators(adjustment.getX() + this.x, adjustment.getY() + this.y,
				AngleUtil.normalize(this.angle + adjustment.getAngle()));
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
			xt += pose.getX();
			yt += pose.getY();
			angles.add(pose.getAngle());
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

	public double getX()
	{
		return x;
	}

	public double getY()
	{
		return y;
	}

	public double getAngle()
	{
		return angle;
	}

	@Override
	public void dumpObservations()
	{
		for (PoseWithMathOperators value : valuesForAverage)
		{
			logger.error("--------> Observation " + value);
		}

	}

}
