package com.pi4j.gpio.extension.lidar;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.config.Config;

import com.pi4j.gpio.extension.adafruit.AdafruitPCA9685;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

public class Lidar implements Runnable
{

	private static final int PWM_PIN = 0;
	private static final int DISTANCE_FROM_CENTER_TO_STRUT = 4;
	private static final int LIDAR_ADDR = 0x62;
	private static final int LIDAR_CONFIG_REGISTER = 0x00;
	private static final int LIDAR_DISTANCE_REGISTER = 0x8f;
	private static final int LIDAR_INTERVAL_REGISTER = 0x45;
	private static final int LIDAR_NUMBER_OF_READINGS_REGISTER = 0x11;

	private static final int MIN_PWM = 170;
	private static final int MAX_PWM = 605;

	private static final int MIN_ANGLE = -90;
	private static final int MAX_ANGLE = 90;

	private static final int STEP_DELAY_MS = 80;
	private static final int STEPS = MAX_ANGLE - MIN_ANGLE;
	private static final int STEP_DURATION = STEP_DELAY_MS;
	private static final int STEP_ANGLE = 4;

	final private I2CDevice lidarDevice;
	private final AdafruitPCA9685 pwm;
	final private ServoAngleToPwmCalculator angleToPwmCalculator;

	private volatile boolean stop = false;

	volatile int resolution = 1;

	private final Map<Integer, Rotation> scanRotationMap = new LinkedHashMap<>();
	private final LidarDataListener listener;
	private Config config;
	
	// think y =mx+c
	private Integer calabrationC;
	private Double calabrationM;

	public Lidar(AdafruitPCA9685 pwm, LidarDataListener listener, Config config)
			throws InterruptedException, IOException
	{
		this.pwm = pwm;
		this.listener = listener;
		this.config = config;
		calabrationC = config.loadSetting("lidar.c", 30);

		calabrationM = config.loadSetting("lidar.m", 1.0);

		// Get I2C bus
		I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends onthe
															// RasPI version

		// Get the device itself
		lidarDevice = bus.getDevice(LIDAR_ADDR);

		setupContinuous(false);
		Thread.sleep(5000);

		angleToPwmCalculator = new ServoAngleToPwmCalculator(MIN_PWM, MAX_PWM,
				MIN_ANGLE, 90);

		init();

		new Thread(this, "lidar").start();

	}

	public void prepareForCalabration()
	{
		calabrationC = 0;
		calabrationM = 1.0;
	}

	public void saveConfig(double m, int c)
	{
		calabrationC = c;
		calabrationM = m;
		config.storeSetting("lidar.c", c);
		config.storeSetting("lidar.m", m);

	}

	void init() throws InterruptedException, IOException
	{

		int min = Integer.MAX_VALUE;

		pwm.setPWM(PWM_PIN, 0,
				(int) angleToPwmCalculator.getPwmValueDegrees(MIN_ANGLE));

		Thread.sleep(500);

		buildScanRotationMap();

	}

	void buildScanRotationMap()
	{
		int i = 0;
		for (i = MIN_ANGLE; i < MAX_ANGLE; i += STEP_ANGLE)
		{
			scanRotationMap.put(i,
					new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(i)));
		}

	}

	@Override
	public void run()
	{

		while (!stop)
		{
			try
			{
				int ctr = 0;
				for (Entry<Integer, Rotation> position : scanRotationMap
						.entrySet())
				{

					if (ctr % resolution == 0)
					{
						int update = update();
						pwm.setPWM(PWM_PIN, 0, (int) angleToPwmCalculator
								.getPwmValueDegrees(position.getKey()));
						if (update > 0 && update < 1000)
						{
							Vector3D vector = new Vector3D(0, update, 0);
							addDataPoint(
									position.getValue().applyInverseTo(vector),
									update, position.getKey());
						}
						Thread.sleep(STEP_DURATION);
					}
				}
				for (int i = MAX_ANGLE; i > MIN_ANGLE; i -= 4)
				{
					pwm.setPWM(PWM_PIN, 0,
							(int) angleToPwmCalculator.getPwmValueDegrees(i));
					Thread.sleep(STEP_DURATION / 2);
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		pwm.setPWM(PWM_PIN, 0, (int) angleToPwmCalculator.getPwmValueDegrees(0));

	}

	private void addDataPoint(Vector3D vector, double distanceCm,
			double angleDegrees)
	{

		listener.addLidarData(vector, distanceCm, angleDegrees);

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
	public int update() throws IOException, InterruptedException
	{
		byte[] distance = new byte[2];

		// Read in 2 bytes from distance register
		lidarDevice.read(LIDAR_DISTANCE_REGISTER, distance, 0, 2);

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

		int value = (d1 * 256) + d2;

		value = (int) ((value * calabrationM) + calabrationC);

		return value;
	}

}
