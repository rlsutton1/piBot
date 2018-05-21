package au.com.rsutton.navigation.graphslam.v3;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.logging.log4j.LogManager;

import au.com.rsutton.angle.AngleUtil;
import au.com.rsutton.angle.WeightedAngle;

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

	List<WeightedPose<PoseWithMathOperators>> valuesForAverage = new LinkedList<>();

	@Override
	public void addWeightedValueForAverage(WeightedPose<PoseWithMathOperators> value)
	{
		valuesForAverage.add(value);

	}

	@Override
	public PoseWithMathOperators getWeightedAverage()
	{
		double xt = 0;
		double yt = 0;

		List<WeightedAngle> angles = new LinkedList<>();

		double totalWeights = 0;
		for (WeightedPose<PoseWithMathOperators> pose : valuesForAverage)
		{
			xt += pose.getPose().getX() * pose.getWeight();
			yt += pose.getPose().getY() * pose.getWeight();
			angles.add(new WeightedAngle(pose.getPose().getAngle(), pose.getWeight()));
			totalWeights += pose.getWeight();
		}

		double averageAngle = AngleUtil.getAverageAngle(angles);

		return new PoseWithMathOperators(xt / totalWeights, yt / totalWeights, averageAngle);
	}

	@Override
	public PoseWithMathOperators copy()
	{
		return new PoseWithMathOperators(x, y, angle);
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
		for (WeightedPose<PoseWithMathOperators> value : valuesForAverage)
		{
			logger.error("--------> Observation " + value.getPose() + " W:" + value.getWeight());
		}
		logger.error("Weighted average angle" + getWeightedAverage().angle);

	}

	@Override
	public double getWeight()
	{
		double total = 0;
		for (WeightedPose<PoseWithMathOperators> value : valuesForAverage)
		{
			total += value.getWeight();
		}
		return total;

	}

	@Override
	public PoseWithMathOperators multiply(double scaler)
	{
		return new PoseWithMathOperators(x * scaler, y * scaler, angle);
	}

	// @Override
	// public PoseWithMathOperators inverse()
	// {
	// return new PoseWithMathOperators(-x, -y, -angle);
	// }

}
