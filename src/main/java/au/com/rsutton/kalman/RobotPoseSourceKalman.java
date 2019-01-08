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

public class RobotPoseSourceKalman implements RobotPoseSource, ParticleFilterListener
{

	private final ParticleFilterIfc pf;
	private Angle heading = new Angle(0, AngleUnits.DEGREES);
	private double stdDev = 0;;
	private ParticleFilterStatus status = ParticleFilterStatus.POOR_MATCH;

	KalmanFilter xFilter = new KalmanFilter();
	KalmanFilter yFilter = new KalmanFilter();

	KalmanFilter hxFilter = new KalmanFilter();
	KalmanFilter hyFilter = new KalmanFilter();

	private final static DistanceUnit D_UNIT = DistanceUnit.CM;

	public RobotPoseSourceKalman(ParticleFilterIfc pf)
	{
		this.pf = pf;
		pf.addListener(this);

	}

	@Override
	public void update(DistanceXY averagePosition, Angle averageHeading, double stdDev,
			List<ScanObservation> particleFilterObservationSet, ParticleFilterStatus status)
	{

		long tick = System.currentTimeMillis();
		KalmanState updateX = new KalmanState(averagePosition.getX().convert(D_UNIT), Math.pow(stdDev, 2));
		xFilter.update(updateX, tick);

		KalmanState updateY = new KalmanState(averagePosition.getY().convert(D_UNIT), Math.pow(stdDev, 2));
		yFilter.update(updateY, tick);

		KalmanState updateHx = new KalmanState(Math.cos(averageHeading.getRadians()), Math.pow(stdDev * 10, 2));
		// hxFilter.estimate(tick);
		hxFilter.update(updateHx, tick);

		KalmanState updateHy = new KalmanState(Math.sin(averageHeading.getRadians()), Math.pow(stdDev * 10, 2));
		// hyFilter.estimate(tick);
		hyFilter.update(updateHy, tick);

		this.stdDev = stdDev;
		this.status = status;

	}

	@Override
	public double getHeading()
	{
		return Math.toDegrees(Math.atan2(hyFilter.getState().getValue(), hxFilter.getState().getValue()));
	}

	@Override
	public DistanceXY getXyPosition()
	{
		return new DistanceXY(xFilter.getState().getValue(), yFilter.getState().getValue(), D_UNIT);
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
