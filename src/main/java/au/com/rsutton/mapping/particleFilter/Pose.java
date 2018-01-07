package au.com.rsutton.mapping.particleFilter;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;

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

}
