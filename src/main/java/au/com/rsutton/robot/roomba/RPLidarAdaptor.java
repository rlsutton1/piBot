package au.com.rsutton.robot.roomba;

import au.com.rsutton.config.Config;
import ev3dev.sensors.slamtec.RPLidarA1;
import ev3dev.sensors.slamtec.RPLidarA1ServiceException;
import ev3dev.sensors.slamtec.RPLidarProvider;
import ev3dev.sensors.slamtec.model.Scan;

public class RPLidarAdaptor implements Runnable
{

	RPLidarProvider lidar = null;
	// configure platform.
	final private RPLidarAdaptorListener listener;

	// publish scan data

	// enact commands

	RPLidarAdaptor(RPLidarAdaptorListener listener)
	{
		this.listener = listener;
	}

	void configure(Config config) throws RPLidarA1ServiceException, InterruptedException
	{

		final String USBPort = config.loadSetting("rplidar usb port", "/dev/ttyUSB0");
		lidar = new RPLidarA1(USBPort);
		lidar.init();

		lidar.continuousScanning();

		new Thread(this).start();

	}

	@Override
	public void run()
	{
		// publish scan data

		while (true)
		{
			try
			{
				Scan scan = lidar.getNextScan();
				listener.receiveLidarScan(scan);

			} catch (RPLidarA1ServiceException | InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	void commandListener()
	{
		// supported commands
		// stop
		// start

	}
}
