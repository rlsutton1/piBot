package au.com.rsutton.robot.roomba;

import ev3dev.sensors.slamtec.model.Scan;

public interface RPLidarAdaptorListener
{

	void receiveLidarScan(Scan scan);

}
