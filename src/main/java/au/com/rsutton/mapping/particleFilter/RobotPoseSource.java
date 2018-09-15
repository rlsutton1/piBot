package au.com.rsutton.mapping.particleFilter;

import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.ui.DataSourcePoint;

public interface RobotPoseSource
{

	public abstract double getHeading();

	public abstract DistanceXY getXyPosition();

	/**
	 * this is implemented by particle filter, generally return a low value
	 * (close to zero but not zero)
	 * 
	 * @return
	 */
	public abstract double getStdDev();

	public abstract DataSourcePoint getParticlePointSource();

	public abstract DataSourceMap getHeadingMapDataSource();

	public abstract ParticleFilterStatus getParticleFilterStatus();

}
