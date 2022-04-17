package au.com.rsutton.units;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class Pose
{

	double x;
	double y;
	double heading;

	public Pose(double x, double y, double heading)
	{
		this.x = x;
		this.y = y;
		this.heading = HeadingHelper.normalizeHeading(heading);
	}

	private Pose(Pose pose)
	{
		x = pose.x;
		y = pose.y;
		heading = pose.heading;
	}

	public Pose addX(double i)
	{
		Pose newPose = new Pose(this);
		newPose.x += i;
		return newPose;
	}

	public Pose addY(double i)
	{
		Pose newPose = new Pose(this);
		newPose.y += i;
		return newPose;
	}

	public Pose addHeading(double i)
	{
		Pose newPose = new Pose(this);
		newPose.heading += i;
		return newPose;
	}

	public double getY()
	{
		return y;
	}

	public double getX()
	{
		return x;
	}

	public double getHeading()
	{
		return heading;
	}

	public Vector3D applyTo(Vector3D vector)
	{
		Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(heading));
		Vector3D delta = new Vector3D(x, y, 0);

		return rotation.applyTo(vector).add(delta);
	}

	@Override
	public String toString()
	{
		return "X: " + x + " Y:" + y + " A:" + heading;

	}

	public Vector3D applyInverseTo(Vector3D source)
	{
		Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(heading));

		Vector3D delta = new Vector3D(x, y, 0);

		return rotation.applyInverseTo(source.subtract(delta));
	}

}
