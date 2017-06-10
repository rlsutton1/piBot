package au.com.rsutton.robot;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.robot.rover.Angle;

public interface RobotListener
{

	void observed(Angle deltaHeading, Distance deltaDistance, RobotLocation robotLocation);

}
