package au.com.rsutton.navigation.feature;

import java.util.List;

import au.com.rsutton.mapping.particleFilter.ScanObservation;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.Distance;

public interface RobotLocationDeltaListener
{

	void onMessage(Angle deltaHeading, Distance deltaDistance, List<ScanObservation> robotLocation);

}
