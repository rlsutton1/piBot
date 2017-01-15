package com.pi4j.gpio.extension.adafruit;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.GpioProviderBase;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import au.com.rsutton.entryPoint.SynchronizedDeviceWrapper;

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

	private final I2CBus bus;
	private final I2CDevice device;

	public static final int CTRL_REG1 = 0x20;
	public static final int CTRL_REG2 = 0x21;
	public static final int CTRL_REG3 = 0x22;
	public static final int CTRL_REG4 = 0x23;

	public static final int Addr = 105; // I2C address of gyro
	volatile double x, y, z;
	volatile double currentX, currentY, currentZ;
	private ScheduledExecutorService worker;
	private volatile double outx;
	private volatile double outy;
	private volatile double outz;

	double xDrift;
	double yDrift;
	double zDrift;
	int driftCnt;
	boolean calabrated = false;
	private Set<GyroListener> gyroListeners = new HashSet<>();

	@Override
	public void run()
	{

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

			if (driftCnt < 500)
			{

				xDrift += x;
				yDrift += y;
				zDrift += z;
				driftCnt++;
				if (driftCnt % 10 == 1)
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
		// correction for gyro after testing with the calabrate dead reconing
		return outz * 1.15;
	}

	public GyroProvider(int busNumber, int address)
			throws IOException, InterruptedException, UnsupportedBusNumberException
	{
		// create I2C communications bus instance
		// default = 0x40
		bus = I2CFactory.getInstance(busNumber);

		// create I2C device instance
		device = new SynchronizedDeviceWrapper(bus.getDevice(address));

		device.write(CTRL_REG1, (byte) 0x1F); // Turn on all axes, disable power
												// down
		device.write(CTRL_REG3, (byte) 0x08); // Enable control ready signal
		device.write(CTRL_REG4, (byte) 0x80); // Set scale (500 deg/sec)
		// delay(100); // Wait to synchronize

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

	public boolean isCalabrated()
	{
		return calabrated;
	}

}
