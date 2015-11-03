package com.pi4j.gpio.extension.lsm303;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import au.com.rsutton.config.Config;
import au.com.rsutton.entryPoint.SynchronizedDeviceWrapper;
import au.com.rsutton.entryPoint.trig.TrigMath;
import au.com.rsutton.i2c.I2cSettings;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.impl.I2CBusImplBanana;

public class CompassLSM303 implements Runnable
{

	private static int SCALE = 2; // accel full-scale, should be 2, 4, or 8

	/* LSM303 Address definitions */
	private static int LSM303_MAG = 0x1E; // assuming SA0 grounded
	private static int LSM303_ACC = 0x18; // assuming SA0 grounded

	private static int X = 0;
	private static int Y = 1;
	private static int Z = 2;

	/* LSM303 Register definitions */
	private static byte CTRL_REG1_A = 0x20;
	private static byte CTRL_REG2_A = 0x21;
	private static byte CTRL_REG3_A = 0x22;
	private static byte CTRL_REG4_A = 0x23;
	private static byte CTRL_REG5_A = 0x24;
	private static byte HP_FILTER_RESET_A = 0x25;
	private static byte REFERENCE_A = 0x26;
	private static byte STATUS_REG_A = 0x27;
	private static byte OUT_X_L_A = 0x28;
	private static byte OUT_X_H_A = 0x29;
	private static byte OUT_Y_L_A = 0x2A;
	private static byte OUT_Y_H_A = 0x2B;
	private static byte OUT_Z_L_A = 0x2C;
	private static byte OUT_Z_H_A = 0x2D;
	private static byte byte1_CFG_A = 0x30;
	private static byte byte1_SOURCE_A = 0x31;
	private static byte byte1_THS_A = 0x32;
	private static byte byte1_DURATION_A = 0x33;
	private static byte CRA_REG_M = 0x00;
	private static byte CRB_REG_M = 0x01;
	private static byte MR_REG_M = 0x02;
	private static byte OUT_X_H_M = 0x03;
	private static byte OUT_X_L_M = 0x04;
	private static byte OUT_Y_H_M = 0x05;
	private static byte OUT_Y_L_M = 0x06;
	private static byte OUT_Z_H_M = 0x07;
	private static byte OUT_Z_L_M = 0x08;
	private static byte SR_REG_M = 0x09;
	private static byte IRA_REG_M = 0x0A;
	private static byte IRB_REG_M = 0x0B;
	private static byte IRC_REG_M = 0x0C;

	/* Global variables */
	int[] accel = new int[3]; // we'll store the raw acceleration values here
	int[] mag = new int[3]; // raw magnetometer values stored here
	float[] realAccel = new float[3]; // calculated acceleration values here

	private I2CBus bus;
	private I2CDevice magDevice;
	private I2CDevice accDevice;

	int maxX = Integer.MIN_VALUE;
	int minX = Integer.MAX_VALUE;
	int maxY = Integer.MIN_VALUE;
	int minY = Integer.MAX_VALUE;

	private CompassAxisCalibractionCorrection yAxisCorrection;

	private CompassAxisCalibractionCorrection xAxisCorrection;

	private Config config;

	public CompassLSM303(Config config)
	{
		try
		{
			this.config = config;
			setup();
			// guarantee initial value is available
			run();
			Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
					this, 200, 200, TimeUnit.MILLISECONDS);
		} catch (IOException | InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void saveConfig()
	{
		config.storeSetting("Compass.min.y", minY);
		config.storeSetting("Compass.max.y", maxY);
		config.storeSetting("Compass.min.x", minX);
		config.storeSetting("Compass.max.x", maxX);

	}

	private void setup() throws IOException, InterruptedException
	{
		minY = config.loadSetting("Compass.min.y", -251);
		maxY = config.loadSetting("Compass.max.y", 108);
		minX = config.loadSetting("Compass.min.x", -271);
		maxX = config.loadSetting("Compass.max.x", 210);

		yAxisCorrection = new CompassAxisCalibractionCorrection(minY, maxY);
		xAxisCorrection = new CompassAxisCalibractionCorrection(minX, maxX);

		// create I2C communications bus instance
		bus = I2CBusImplBanana.getBus(I2cSettings.busNumber);

		// create I2C device instance
		magDevice = new SynchronizedDeviceWrapper(bus.getDevice(LSM303_MAG));
		accDevice = new SynchronizedDeviceWrapper(bus.getDevice(LSM303_ACC));

		initLSM303(SCALE); // Initialize the LSM303, using a SCALE full-scale
							// range

	}

	private void loop() throws IOException, InterruptedException
	{
		getLSM303_accel(accel); // get the acceleration values and store them in
								// the accel array
		// while(!(byte)(LSM303_read(SR_REG_M) & 0x01))
		// ; // wait for the magnetometer readings to be ready
		getLSM303_mag(mag); // get the magnetometer values, store them in mag
		// printValues(mag, accel); // print the raw accel and mag values, good
		// debugging

		for (int i = 0; i < 3; i++)
			realAccel[i] = (float) (accel[i] / Math.pow(2, 15) * SCALE); // calculate
																			// real
																			// acceleration
																			// values,
																			// in
																			// units
																			// of
																			// g

		/* print both the level, and tilt-compensated headings below to compare */
		// System.out.println(getHeading(mag) + " " + mag[Y] + " " + mag[X]); //
		// this
		// only
		// works
		// if
		// the
		// sensor
		// is
		// level

		// for calabration
		// System.out.println("x" + maxX + " -> " + minX + " y" + maxY + " -> "
		// + minY);

		// TODO: fix the accelerometer code, probably math issues, havent looked
		// yet though
		// System.out.println("\t\t"); // print some tabs
		// System.out.println(getTiltHeading(mag, realAccel)); // see how
		// awesome
		// tilt compensation
		// is?!

		// printValues(mag, accel);
		Thread.sleep(100); // delay for serial readability
	}

	private void initLSM303(int fs) throws IOException
	{
		accDevice.write(CTRL_REG1_A, (byte) 0x27); // 0x27 = normal power mode,
													// all accel axes on
		if ((fs == 8) || (fs == 4))
			magDevice
					.write(CTRL_REG4_A, (byte) (0x00 | (fs - fs / 2 - 1) << 4)); // set
																					// full-scale
		else
			magDevice.write(CTRL_REG4_A, (byte) 0x00);
		magDevice.write(CRA_REG_M, (byte) 0x14); // 0x14 = mag 30Hz output rate
		magDevice.write(MR_REG_M, (byte) 0x00); // 0x00 = continouous conversion
												// mode
	}

	private void printValues(int[] magArray, int[] accelArray)
	{
		/* print out mag and accel arrays all pretty-like */
		System.out.println(accelArray[X]);
		System.out.println("\t");
		System.out.println(accelArray[Y]);
		System.out.println("\t");
		System.out.println(accelArray[Z]);
		System.out.println("\t\t");

		System.out.println(magArray[X]);
		System.out.println("\t");
		System.out.println(magArray[Y]);
		System.out.println("\t");
		System.out.println(magArray[Z]);
		System.out.println("");
	}

	final static AtomicReference<Float> heading = new AtomicReference<>();
	final static long TIME_TO_LIVE = 100;
	volatile long lastReadTime = 0;

	public float getHeading() throws IOException
	{
		return heading.get();
	}

	public void run()
	{
		try
		{
			List<Double> angles = new LinkedList<>();
			int max = Integer.MIN_VALUE;
			int min = Integer.MAX_VALUE;
			for (int i = 0; i < 15; i++)
			{
				double internalGetHeading = internalGetHeading();
				max = (int) Math.max(max, internalGetHeading);
				min = (int) Math.min(min, internalGetHeading);
				angles.add(internalGetHeading);

				Thread.sleep(5);

			}

			if (max == min)
			{
				// the compass probably crashed!

				System.out
						.println("compass crashed, rebooting it !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				initLSM303(SCALE);
			} else
			{
				heading.set((float) TrigMath.averageAngles(angles));
			}
			dumpCalabrationData();
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	long lastDumpTime = 0;

	private int calabrationStartX;

	private int calabrationStartY;

	private void dumpCalabrationData()
	{
		long currentTimeMillis = System.currentTimeMillis();
		if (currentTimeMillis > lastDumpTime + 60000)
		{
			lastDumpTime = currentTimeMillis;
			System.out.println("Dump compass calabration data X: " + minX + " "
					+ maxX + " Y: " + minY + " " + maxY);
		}
	}

	/**
	 * 
	 * @return - heading in degrees
	 * @throws IOException
	 */
	private double internalGetHeading() throws IOException
	{

		getLSM303_mag(mag);

		// capture data for calabration purposes
		maxX = Math.max(maxX, mag[X]);
		minX = Math.min(minX, mag[X]);
		maxY = Math.max(maxY, mag[Y]);
		minY = Math.min(minY, mag[Y]);

		// System.out.println("Raw " + mag[X] + " " + mag[Y]);

		// make calabration adjustments
		mag[X] = xAxisCorrection.getCorrectedValue(mag[X]);
		mag[Y] = yAxisCorrection.getCorrectedValue(mag[Y]);

		// see section 1.2 in app note AN3192
		double heading = Math.toDegrees(Math.atan2(mag[Y], mag[X])); // assume
																		// pitch,
																		// roll
																		// are
																		// 0

		// cap angle at 360 degrees
		if (heading < 0)
			heading += 360;

		// adjust for orientation of compass in robot
		heading -= 90;
		if (heading > 360)
			heading -= 360;

		return heading;
	}

	private float getTiltHeading(int[] magValue, float[] accelValue)
	{
		// see appendix A in app note AN3192
		float pitch = (float) Math.asin(-accelValue[X]);
		float roll = (float) Math.asin(accelValue[Y] / Math.cos(pitch));

		float xh = (float) (magValue[X] * Math.cos(pitch) + magValue[Z]
				* Math.sin(pitch));
		float yh = (float) (magValue[X] * Math.sin(roll) * Math.sin(pitch)
				+ magValue[Y] * Math.cos(roll) - magValue[Z] * Math.sin(roll)
				* Math.cos(pitch));
		float zh = (float) (-magValue[X] * Math.cos(roll) * Math.sin(pitch)
				+ magValue[Y] * Math.sin(roll) + magValue[Z] * Math.cos(roll)
				* Math.cos(pitch));

		float heading = (float) (180 * Math.atan2(yh, xh) / Math.PI);
		if (yh >= 0)
			return heading;
		else
			return (360 + heading);
	}

	private void getLSM303_mag(int[] rawValues) throws IOException
	{
		magDevice.write((byte) OUT_X_H_M);
		byte[] bytes = new byte[6];
		magDevice.read(bytes, 0, 6);

		for (int i = 0; i < 3; i++)
		{
			byte msb = bytes[i * 2];
			byte lsb = bytes[(i * 2) + 1];

			rawValues[i] = convertBytesToInt(msb, lsb);
		}
	}

	private int convertBytesToInt(int msb, int lsb)
	{
		int value = msb * 256;

		if (lsb < 0)
		{
			// lsb should be unsigned
			value += 256;
		}
		value += lsb;
		return value;
	}

	private void getLSM303_accel(int[] rawValues) throws IOException
	{
		rawValues[Z] = convertBytesToInt(accDevice.read(OUT_X_L_A),
				accDevice.read(OUT_X_H_A));
		rawValues[X] = convertBytesToInt(accDevice.read(OUT_Y_L_A),
				accDevice.read(OUT_Y_H_A));
		rawValues[Y] = convertBytesToInt(accDevice.read(OUT_Z_L_A),
				accDevice.read(OUT_Z_H_A));
		// had to swap those to right the data with the proper axis
	}

	/**
	 * mark our orientation, so we know when calabration is over
	 */
	public void startCalabration()
	{
		minX = Integer.MAX_VALUE;
		maxX = Integer.MIN_VALUE;
		minY = Integer.MAX_VALUE;
		maxY = Integer.MIN_VALUE;

		calabrationStartX = mag[X];
		calabrationStartY = mag[Y];
	}

	/**
	 * wait up to 30 seconds for the compass to return to it's starting position
	 * before accepting the calabration data
	 * 
	 * @throws InterruptedException
	 */
	public void finishcalabration() throws InterruptedException
	{
		long start = System.currentTimeMillis();
		while (!calabrationFinished()
				&& System.currentTimeMillis() - start < 30000)
		{
			Thread.sleep(1000);
		}

		yAxisCorrection = new CompassAxisCalibractionCorrection(minY, maxY);
		xAxisCorrection = new CompassAxisCalibractionCorrection(minX, maxX);

	}

	/**
	 * check if we've rotated back to the position we were at calabration start
	 * 
	 * @return
	 */
	private boolean calabrationFinished()
	{
		return Math.abs(calabrationStartX - mag[X]) < 30
				&& Math.abs(calabrationStartY - mag[Y]) < 30;
	}

}
