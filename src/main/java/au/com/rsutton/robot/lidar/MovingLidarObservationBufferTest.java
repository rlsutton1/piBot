package au.com.rsutton.robot.lidar;

import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;

import au.com.rsutton.mapping.particleFilter.ParticleFilterStatus;
import au.com.rsutton.mapping.particleFilter.RobotPoseSource;
import au.com.rsutton.mapping.particleFilter.ScanObservation;
import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.ui.DataSourcePoint;
import au.com.rsutton.units.DistanceUnit;

public class MovingLidarObservationBufferTest
{

	double x = 0;
	double y = 0;
	double heading = 0;

	@Test
	public void test()
	{

		MovingLidarObservationBuffer buffer = new MovingLidarObservationBuffer(getPoseSource());

		List<ScanObservation> observations = new LinkedList<>();
		observations.add(getTestObservation());
		buffer.addLidarObservation(observations);

		y += 10;
		List<LidarObservation> result = buffer.getTranslatedObservations();
		assertTrue(result.get(0).getY() == 0);

		x += 10;

		result = buffer.getTranslatedObservations();
		assertTrue(result.get(0).getY() == 0);
		assertTrue(result.get(0).getX() == -10);

		y = 0;
		x = 0;
		heading += 90;

		result = buffer.getTranslatedObservations();
		System.out.println(result.get(0).getY());
		System.out.println(result.get(0).getX());

		assertTrue(result.get(0).getY() == 0);
		assertTrue(result.get(0).getX() == 10);

	}

	@Test
	public void test2()
	{

		MovingLidarObservationBuffer buffer = new MovingLidarObservationBuffer(getPoseSource());

		heading = 90;

		List<ScanObservation> observations = new LinkedList<>();
		observations.add(getTestObservation());
		buffer.addLidarObservation(observations);

		heading = 0;

		List<LidarObservation> result = buffer.getTranslatedObservations();

		System.out.println("t2 " + result.get(0).getY());
		System.out.println(result.get(0).getX());

		assertTrue(result.get(0).getY() == 0);
		assertTrue(result.get(0).getX() == -10);

	}

	private ScanObservation getTestObservation()
	{
		return new ScanObservation()
		{

			@Override
			public int getY()
			{
				return 10;
			}

			@Override
			public int getX()
			{
				return 0;
			}

			@Override
			public Vector3D getVector()
			{
				return new Vector3D(getX(), getY(), 0);
			}

			@Override
			public double getDisctanceCm()
			{
				return 10;
			}

			@Override
			public double getAngleRadians()
			{
				return 0;
			}
		};
	}

	private RobotPoseSource getPoseSource()
	{
		return new RobotPoseSource()
		{

			@Override
			public DistanceXY getXyPosition()
			{
				return new DistanceXY(x, y, DistanceUnit.CM);
			}

			@Override
			public double getStdDev()
			{
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public double getHeading()
			{
				return heading;
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
			public ParticleFilterStatus getParticleFilterStatus()
			{
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

}
