package au.com.rsutton.robot;

import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.Distance;

public interface RobotListener
{

	void observed(Angle deltaHeading, Distance deltaDistance, RobotLocation robotLocation);

}
