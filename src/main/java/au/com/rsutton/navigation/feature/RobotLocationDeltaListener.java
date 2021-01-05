package au.com.rsutton.navigation.feature;

import au.com.rsutton.hazelcast.LidarScan;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.Distance;

public interface RobotLocationDeltaListener
{

	void onMessage(Angle deltaHeading, Distance deltaDistance, boolean bump, Distance absoluteTotalDistance);

	void onMessage(LidarScan scan);

}
