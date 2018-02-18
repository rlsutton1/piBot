package au.com.rsutton.mapping.particleFilter;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.ui.DataSourcePoint;
import au.com.rsutton.units.DistanceUnit;

public class PoseAdjuster implements RobotPoseSource

{
	Pose pose;
	RobotPoseSource source;

	PoseAdjuster(Pose pose, RobotPoseSource source)
	{
		this.pose = pose;
		this.source = source;
	}

	@Override
	public double getHeading()
	{
		return pose.heading + source.getHeading();
	}

	@Override
	public DistanceXY getXyPosition()
	{
		Vector3D vector = new Vector3D(source.getXyPosition().getX().convert(DistanceUnit.CM),
				source.getXyPosition().getY().convert(DistanceUnit.CM), 0);

		vector = pose.applyTo(vector);

		return new DistanceXY(vector.getX(), vector.getY(), DistanceUnit.CM);
	}

	@Override
	public double getStdDev()
	{
		return source.getStdDev();
	}

	@Override
	public void shutdown()
	{
		source.shutdown();

	}

	public void setPose(Pose mapPose)
	{
		pose = mapPose;

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

}
