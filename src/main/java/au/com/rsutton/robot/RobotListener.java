package au.com.rsutton.robot;

import au.com.rsutton.hazelcast.RobotLocation;

public interface RobotListener
{

	void observed(RobotLocation observation);

}
