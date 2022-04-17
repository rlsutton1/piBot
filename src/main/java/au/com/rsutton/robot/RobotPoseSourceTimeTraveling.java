package au.com.rsutton.robot;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import au.com.rsutton.hazelcast.LidarScan;
import au.com.rsutton.mapping.particleFilter.ParticleFilterIfc;
import au.com.rsutton.mapping.particleFilter.ParticleFilterListener;
import au.com.rsutton.mapping.particleFilter.ParticleFilterStatus;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.AngleUnits;
import au.com.rsutton.units.DistanceUnit;
import au.com.rsutton.units.DistanceXY;
import au.com.rsutton.units.HeadingHelper;

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
		RobotPoseInstant instant1 = null;
		RobotPoseInstant instant2 = null;

		for (RobotPoseInstant instant : instants)
		{
			if (instant.time >= time)
			{
				if (instant1 == null)
				{
					instant1 = instant;
				} else if (instant2 == null)
				{
					instant2 = instant;
				} else
				{
					break;
				}
			}
		}
		if (instant1 == null)
		{
			return current;
		}
		if (instant2 == null)
		{
			return instant1;
		}

		double x1 = instant1.getXyPosition().getX().convert(DistanceUnit.CM);
		double y1 = instant1.getXyPosition().getY().convert(DistanceUnit.CM);
		double h1 = instant1.getHeading();

		double x2 = instant2.getXyPosition().getX().convert(DistanceUnit.CM);
		double y2 = instant2.getXyPosition().getY().convert(DistanceUnit.CM);
		double h2 = instant2.getHeading();

		double duration = instant2.time - instant1.time;

		double s1 = (time - instant1.time) / duration;
		double s2 = 1.0 - s1;

		double x = (x1 * (s1)) + (x2 * (s2));
		double y = (y1 * (s1)) + (y2 * (s2));

		double change = HeadingHelper.getChangeInHeading(h1, h2);
		double h = HeadingHelper.normalizeHeading(h1 + (change * (s1)));

		RobotPoseInstant ii = new RobotPoseInstant();
		ii.heading = new Angle(h, AngleUnits.DEGREES);
		ii.position = new DistanceXY(x, y, DistanceUnit.CM);
		ii.status = instant2.status;
		ii.stdDev = instant2.stdDev;

		return ii;

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
