package au.com.rsutton.mapping.particleFilter;

import au.com.rsutton.navigation.feature.DistanceXY;

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

	public abstract ParticleFilterStatus getParticleFilterStatus();

}
