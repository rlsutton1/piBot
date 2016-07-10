package au.com.rsutton.mapping.particleFilter;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public interface ParticleFilterListener
{

	void update(Vector3D averagePosition, double averageHeading, double stdDev,
			ParticleFilterObservationSet particleFilterObservationSet);

}
