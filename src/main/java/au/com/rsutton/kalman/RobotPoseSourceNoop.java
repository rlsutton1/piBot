package au.com.rsutton.kalman;

import java.util.List;

import au.com.rsutton.mapping.particleFilter.ParticleFilterIfc;
import au.com.rsutton.mapping.particleFilter.ParticleFilterListener;
import au.com.rsutton.mapping.particleFilter.ParticleFilterStatus;
import au.com.rsutton.mapping.particleFilter.RobotPoseSource;
import au.com.rsutton.mapping.particleFilter.ScanObservation;
import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.ui.DataSourcePoint;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.AngleUnits;
import au.com.rsutton.units.DistanceUnit;

public class RobotPoseSourceNoop implements RobotPoseSource, ParticleFilterListener
{

	private final ParticleFilterIfc pf;
	private DistanceXY position = new DistanceXY(0, 0, DistanceUnit.CM);
	private Angle heading = new Angle(0, AngleUnits.DEGREES);
	private double stdDev = 0;;
	private ParticleFilterStatus status = ParticleFilterStatus.POOR_MATCH;

	public RobotPoseSourceNoop(ParticleFilterIfc pf)
	{
		this.pf = pf;
		pf.addListener(this);

	}

	@Override
	public void update(DistanceXY averagePosition, Angle averageHeading, double stdDev,
			List<ScanObservation> particleFilterObservationSet, ParticleFilterStatus status)
	{
		this.position = averagePosition;
		this.heading = averageHeading;
		this.stdDev = stdDev;
		this.status = status;

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
	public DataSourcePoint getParticlePointSource()
	{
		return pf.getParticlePointSource();
	}

	@Override
	public DataSourceMap getHeadingMapDataSource()
	{
		return pf.getHeadingMapDataSource();
	}

	@Override
	public ParticleFilterStatus getParticleFilterStatus()
	{
		return status;
	}
}
