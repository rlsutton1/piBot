package au.com.rsutton.robot.lidar;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import au.com.rsutton.config.Config;
import au.com.rsutton.entryPoint.SynchronizedDeviceWrapper;

public class LidarLite
{

	private static final int LIDAR_ADDR = 0x62;
	private static final int LIDAR_CONFIG_REGISTER = 0x00;
	private static final int LIDAR_DISTANCE_REGISTER = 0x8f;
	private static final int LIDAR_INTERVAL_REGISTER = 0x45;
	private static final int LIDAR_NUMBER_OF_READINGS_REGISTER = 0x11;

	final private I2CDevice lidarDevice;

	volatile int resolution = 1;

	// think y =mx+c
	private Integer calabrationC;
	private Double calabrationM;

	public LidarLite(Config config) throws InterruptedException, IOException, UnsupportedBusNumberException
	{
		calabrationC = config.loadSetting("lidar.c", 30);

		calabrationM = config.loadSetting("lidar.m", 1.0);

		// Get I2C bus
		I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends onthe
															// RasPI version

		// Get the device itself
		lidarDevice = new SynchronizedDeviceWrapper(bus.getDevice(LIDAR_ADDR));

		setupContinuous(false);

	}

	void setCalabrationC(double c)
	{
		calabrationC = (int) c;
	}

	void setupContinuous(boolean modePinLow) throws IOException
	{
		// Register 0x45 sets the time between measurements. 0xc8 corresponds to
		// 10Hz
		// while 0x13 corresponds to 100Hz. Minimum value is 0x02 for proper
		// operation.
		lidarDevice.write(LIDAR_INTERVAL_REGISTER, (byte) 0x13);
		// Set register 0x04 to 0x20 to look at "NON-default" value of velocity
		// scale
		// If you set bit 0 of 0x04 to "1" then the mode pin will be low when
		// done
		if (modePinLow)
		{
			lidarDevice.write(0x04, (byte) 0x21);
		} else
		{
			lidarDevice.write(0x04, (byte) 0x20);
		}
		// Set the number of readings, 0xfe = 254 readings, 0x01 = 1 reading and
		// 0xff = continuous readings
		lidarDevice.write(LIDAR_NUMBER_OF_READINGS_REGISTER, (byte) 0xff);
		// Initiate reading distance
		lidarDevice.write(LIDAR_CONFIG_REGISTER, (byte) 0x04);
	}

	// Update distance variable
	public int getLatestReading() throws IOException, InterruptedException
	{
		byte[] distance = new byte[2];

		int value = 0;
		// Read in 2 bytes from distance register
		try
		{
			lidarDevice.read(LIDAR_DISTANCE_REGISTER, distance, 0, 2);

		} catch (IOException e)
		{
			Thread.sleep(20);
			lidarDevice.read(LIDAR_DISTANCE_REGISTER, distance, 0, 2);
		}
		int d1 = distance[0];
		if (d1 < 0)
		{
			d1 += 256;
		}
		int d2 = distance[1];
		if (d2 < 0)
		{
			d2 += 256;
		}

		value = (d1 * 256) + d2;

		value = (int) ((value * calabrationM) + calabrationC);
		return value;
	}

}
