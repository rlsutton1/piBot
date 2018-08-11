package au.com.rsutton.mapping.particleFilter;

import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.ui.DataSourcePoint;
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
	public void shutdown()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public DataSourcePoint getParticlePointSource()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataSourceMap getHeadingMapDataSource()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getBestScanMatchScore()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getBestRawScore()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ParticleFilterStatus getParticleFilterStatus()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
