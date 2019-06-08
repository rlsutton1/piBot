package au.com.rsutton.mapping.particleFilter;

import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.units.DistanceUnit;

public class DummySource implements RobotPoseSource
{

	private Pose pose;

	public DummySource(Pose pose)
	{
		this.pose = pose;
	}

	@Override
	public double getHeading()
	{
		return pose.heading;
	}

	@Override
	public DistanceXY getXyPosition()
	{
		return new DistanceXY(pose.x, pose.y, DistanceUnit.CM);
	}

	@Override
	public double getStdDev()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ParticleFilterStatus getParticleFilterStatus()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
