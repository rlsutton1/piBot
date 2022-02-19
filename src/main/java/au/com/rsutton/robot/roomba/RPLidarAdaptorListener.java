package au.com.rsutton.robot.roomba;

import ev3dev.sensors.slamtec.model.Scan;

interface RPLidarAdaptorListener
{

	void receiveLidarScan(Scan scan);

}
