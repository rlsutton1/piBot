package au.com.rsutton.mapping.particleFilter;

import au.com.rsutton.hazelcast.LidarScan;
import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.units.Angle;

public interface ParticleFilterListener
{

	void update(DistanceXY averagePosition, Angle averageHeading, double stdDev, LidarScan particleFilterObservationSet,
			ParticleFilterStatus status);

}
