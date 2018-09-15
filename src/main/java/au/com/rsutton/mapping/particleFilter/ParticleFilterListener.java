package au.com.rsutton.mapping.particleFilter;

import java.util.List;

import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.units.Angle;

public interface ParticleFilterListener
{

	void update(DistanceXY averagePosition, Angle averageHeading, double stdDev,
			List<ScanObservation> particleFilterObservationSet, ParticleFilterStatus status);

}
