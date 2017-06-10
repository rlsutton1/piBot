package au.com.rsutton.navigation.feature;

import java.util.List;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.mapping.particleFilter.ScanObservation;
import au.com.rsutton.robot.rover.Angle;

public interface RobotLocationDeltaListener
{

	void onMessage(Angle deltaHeading, Distance deltaDistance, List<ScanObservation> robotLocation);

}
