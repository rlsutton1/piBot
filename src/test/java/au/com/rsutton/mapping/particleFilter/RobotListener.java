package au.com.rsutton.mapping.particleFilter;

import au.com.rsutton.hazelcast.RobotLocation;

public interface RobotListener
{

	void observed(RobotLocation observation);

}
