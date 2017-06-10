package au.com.rsutton.mapping.particleFilter;

import java.util.List;

import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.robot.rover.Angle;

public interface ParticleFilterListener
{

	void update(DistanceXY averagePosition, Angle averageHeading, double stdDev,
			List<ScanObservation> particleFilterObservationSet);

}
