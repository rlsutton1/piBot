package au.com.rsutton.robot.roomba;

import au.com.rsutton.config.Config;
import ev3dev.sensors.slamtec.RPLidarA1;
import ev3dev.sensors.slamtec.RPLidarA1ServiceException;
import ev3dev.sensors.slamtec.RPLidarProvider;
import ev3dev.sensors.slamtec.model.Scan;

public class RPLidarAdaptor implements Runnable
{

	public static final String RPLIDAR_USB_PORT = "rplidar usb port";
	RPLidarProvider lidar = null;
	// configure platform.
	final private RPLidarAdaptorListener listener;
	private volatile boolean stop;

	// publish scan data

	// enact commands

	RPLidarAdaptor(RPLidarAdaptorListener listener)
	{
		this.listener = listener;
	}

	void configure(Config config, String port) throws RPLidarA1ServiceException, InterruptedException
	{

		String USBPort = config.loadSetting(RPLIDAR_USB_PORT, "/dev/ttyUSB0");
		System.out.println("Using " + USBPort);

		lidar = new RPLidarA1(USBPort);
		lidar.init();

		lidar.continuousScanning();

		new Thread(this).start();

	}

	@Override
	public void run()
	{
		// publish scan data

		while (!stop)
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

	public void shutdown()
	{
		stop = true;
		try
		{
			lidar.close();
		} catch (RPLidarA1ServiceException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
