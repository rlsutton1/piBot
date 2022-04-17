package au.com.rsutton.robot;

import au.com.rsutton.mapping.particleFilter.ParticleFilterStatus;
import au.com.rsutton.units.DistanceXY;

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
