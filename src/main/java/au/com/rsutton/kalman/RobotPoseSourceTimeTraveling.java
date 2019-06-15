package au.com.rsutton.kalman;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import au.com.rsutton.hazelcast.LidarScan;
import au.com.rsutton.mapping.particleFilter.ParticleFilterIfc;
import au.com.rsutton.mapping.particleFilter.ParticleFilterListener;
import au.com.rsutton.mapping.particleFilter.ParticleFilterStatus;
import au.com.rsutton.mapping.particleFilter.RobotPoseSource;
import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.AngleUnits;
import au.com.rsutton.units.DistanceUnit;

public class RobotPoseSourceTimeTraveling implements RobotPoseSource, ParticleFilterListener
{

	public static class RobotPoseInstant implements RobotPoseSource
	{
		private DistanceXY position = new DistanceXY(0, 0, DistanceUnit.CM);
		private Angle heading = new Angle(0, AngleUnits.DEGREES);
		private double stdDev = 0;;
		private ParticleFilterStatus status = ParticleFilterStatus.POOR_MATCH;
		public long time;

		RobotPoseInstant copy()
		{
			RobotPoseInstant clone = new RobotPoseInstant();
			clone.position = position;
			clone.heading = heading;
			clone.stdDev = stdDev;
			clone.status = status;
			clone.time = time;
			return clone;
		}

		@Override
		public double getHeading()
		{
			return heading.getDegrees();
		}

		@Override
		public DistanceXY getXyPosition()
		{
			return position;
		}

		@Override
		public double getStdDev()
		{
			return stdDev;
		}

		@Override
		public ParticleFilterStatus getParticleFilterStatus()
		{
			return status;
		};

	}

	private final List<RobotPoseInstant> instants = new CopyOnWriteArrayList<>();

	RobotPoseInstant current = new RobotPoseInstant();

	public RobotPoseSourceTimeTraveling(ParticleFilterIfc pf)
	{
		pf.addListener(this);

	}

	@Override
	public void update(DistanceXY averagePosition, Angle averageHeading, double stdDev,
			LidarScan particleFilterObservationSet, ParticleFilterStatus status)
	{
		current.position = averagePosition;
		current.heading = averageHeading;
		current.stdDev = stdDev;
		current.status = status;
		current.time = particleFilterObservationSet.getEndTime();

		RobotPoseInstant instant = current.copy();
		instants.add(instant);
		if (instants.size() > 100)
		{
			instants.remove(0);
		}

	}

	public RobotPoseInstant findInstant(long time)
	{

		for (RobotPoseInstant instant : instants)
		{
			if (instant.time >= time)
			{
				// TODO: interpolate adjacent instants
				return instant;
			}
		}
		return current;
	}

	@Override
	public double getHeading()
	{
		return current.heading.getDegrees();
	}

	@Override
	public DistanceXY getXyPosition()
	{
		return current.position;
	}

	@Override
	public double getStdDev()
	{
		return current.stdDev;
	}

	@Override
	public ParticleFilterStatus getParticleFilterStatus()
	{
		return current.status;
	}
}