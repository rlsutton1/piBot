package com.pi4j.gpio.extension.adafruit;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import au.com.rsutton.entryPoint.SynchronizedDeviceWrapper;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.GpioProviderBase;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.impl.I2CBusImplBanana;

/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: GPIO Extension
 * FILENAME      :  MCP23008GpioProvider.java  
 * 
 * This file is part of the Pi4J project. More information about 
 * this project can be found here:  http://www.pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 Pi4J
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/**
 * <p>
 * This GPIO provider implements the MCP23008 I2C GPIO expansion board as native
 * Pi4J GPIO pins. More information about the board can be found here: *
 * http://ww1.microchip.com/downloads/en/DeviceDoc/21919e.pdf
 * http://learn.adafruit.com/mcp230xx-gpio-expander-on-the-raspberry-pi/overview
 * </p>
 * 
 * <p>
 * The MCP23008 is connected via I2C connection to the Raspberry Pi and provides
 * 8 GPIO pins that can be used for either digital input or digital output pins.
 * </p>
 * 
 * @author Robert Savage
 * 
 */
public class GyroProvider extends GpioProviderBase implements GpioProvider, Runnable
{
	public static final String NAME = GyroProvider.class.getCanonicalName();
	public static final String DESCRIPTION = "GYRO";

	final private I2CBus bus;
	final private I2CDevice device;

	public static final int CTRL_REG1 = 0x20;
	public static final int CTRL_REG2 = 0x21;
	public static final int CTRL_REG3 = 0x22;
	public static final int CTRL_REG4 = 0x23;
	private static final int SECOND = 1000;

	public static final int Addr = 105; // I2C address of gyro
	volatile double x, y, z;
	volatile double currentX, currentY, currentZ;
	// int ctr = 50;
	private ScheduledExecutorService worker;
	volatile private double outx;
	volatile private double outy;
	volatile private double outz;

	double xDrift;
	double yDrift;
	double zDrift;
	int driftCnt;
	private long calabrationStart;
	boolean calabrated = false;
	private Set<GyroListener> gyroListeners = new HashSet<GyroListener>();

	@Override
	public void run()
	{
		// TODO Auto-generated method stub

		try
		{
			int MSB, LSB;

			MSB = device.read(0x29);
			LSB = device.read(0x28);
			x = ((MSB << 8) | LSB);

			MSB = device.read(0x2B);
			LSB = device.read(0x2A);
			y = ((MSB << 8) | LSB);

			MSB = device.read(0x2D);
			LSB = device.read(0x2C);
			z = ((MSB << 8) | LSB);

			if (x > 0x7FFF)
			{
				x = x - 0xFFFF;
			}
			if (y > 0x7FFF)
			{
				y = y - 0xFFFF;
			}

			if (z > 0x7FFF)
			{
				z = z - 0xFFFF;
			}

			double scaling = 11000;
			double filter = 20;

			x = (x / (filter));
			y = (y / (filter));
			z = (z / (filter));

			double scaler = scaling / filter;

			if (calabrationStart + (10 * SECOND) > System.currentTimeMillis() || driftCnt < 10)
			{

				xDrift += x;
				yDrift += y;
				zDrift += z;
				driftCnt++;
				if (driftCnt % 100 == 1)
				{
					System.out.println("calabrating...");
				}
			} else
			{
				calabrated = true;
				currentX += (x - (xDrift / driftCnt));
				currentY += (y - (yDrift / driftCnt));
				currentZ += (z - (zDrift / driftCnt));

			}

			outx = (currentX / scaler);
			outy = (currentY / scaler);
			outz = (currentZ / scaler);

			for (GyroListener listener : gyroListeners)
			{
				listener.gyroChanged(outx, outy, outz);
			}
		} catch (IOException i)
		{
			i.printStackTrace();
		}
	}

	public void addHeadingListener(GyroListener listener)
	{
		gyroListeners.add(listener);
	}

	public double getHeading()
	{
		return outz;
	}

	public GyroProvider(int busNumber, int address) throws IOException, InterruptedException
	{
		// create I2C communications bus instance
		// default = 0x40
		bus = I2CBusImplBanana.getBus(busNumber);

		// create I2C device instance
		device = new SynchronizedDeviceWrapper(bus.getDevice(address));

		device.write(CTRL_REG1, (byte) 0x1F); // Turn on all axes, disable power
												// down
		device.write(CTRL_REG3, (byte) 0x08); // Enable control ready signal
		device.write(CTRL_REG4, (byte) 0x80); // Set scale (500 deg/sec)
		// delay(100); // Wait to synchronize

		calabrationStart = System.currentTimeMillis();
		worker = GpioFactory.getExecutorServiceFactory().getScheduledExecutorService();

		worker.scheduleAtFixedRate(this, 10, 10, TimeUnit.MILLISECONDS);
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public void shutdown()
	{

		worker.shutdown();

	}

}
