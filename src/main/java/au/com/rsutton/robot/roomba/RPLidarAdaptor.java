package au.com.rsutton.robot.roomba;

import au.com.rsutton.config.Config;
import au.com.rsutton.hazelcast.DataLogLevel;
import au.com.rsutton.hazelcast.DataLogValue;
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

		System.out.println("Start listening for scans");

		try
		{
			int badScans = 0;
			while (!stop)
			{
				try
				{
					Scan scan = lidar.getNextScan();
					if (scan == null)
					{
						badScans++;
						if (badScans > 2)
						{
							lidar.close();
							lidar.init();
							lidar.forceContinuousScanning();
							new DataLogValue("Force Lidar Scan", "yes", DataLogLevel.WARN).publish();
							badScans = 0;
						}
					} else
					{
						System.out.println("Scan Received");
						listener.receiveLidarScan(scan);
					}
				} catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} finally
		{
			System.out.println("Continous scanning exiting...");
		}

	}

	public void shutdown()
	{
		System.out.println("RPLidarAdaptor - SHUTDOWN called");
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
