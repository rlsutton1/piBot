package com.pi4j.gpio.extension.adafruit;

import java.io.IOException;

import au.com.rsutton.entryPoint.SynchronizedDeviceWrapper;

import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.GpioProviderBase;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.exception.InvalidPinException;
import com.pi4j.io.gpio.exception.UnsupportedPinModeException;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

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
public class Adafruit16PwmProvider extends GpioProviderBase implements GpioProvider
{
	public static final String NAME = Adafruit16PwmProvider.class.getCanonicalName();
	public static final String DESCRIPTION = "Adafruit 16 channel PWM Provider";

	private static final int MODE1 = 0x00;
	private static final int PRESCALE = 0xFE;
	private static final int LED0_ON_L = 0x06;
	private static final int LED0_ON_H = 0x07;
	private static final int LED0_OFF_L = 0x08;
	private static final int LED0_OFF_H = 0x09;

	// __SUBADR1 = 0x02
	// __SUBADR2 = 0x03
	// __SUBADR3 = 0x04
	// __ALLLED_ON_L = 0xFA
	// __ALLLED_ON_H = 0xFB
	// __ALLLED_OFF_L = 0xFC
	// __ALLLED_OFF_H = 0xFD

	public static final int REGISTER_IODIR = 0x00;
	private static final int REGISTER_DEFVAL = 0x03;
	private static final int REGISTER_INTCON = 0x04;
	public static final int REGISTER_GPIO = 0x09;

	private I2CBus bus;
	private I2CDevice device;

	public Adafruit16PwmProvider(int busNumber, int address) throws IOException, InterruptedException
	{
		// create I2C communications bus instance
		// default = 0x40
		bus = I2CFactory.getInstance(busNumber);

		// create I2C device instance
		device = new SynchronizedDeviceWrapper(bus.getDevice(address));

		// reset PCA9685
		device.write(MODE1, (byte) 0x00);

		// set all default pin interrupt comparison behaviors
		device.write(REGISTER_INTCON, (byte) 0x00);

		// safe default for analogue servos
		setPWMFreq(25);


	}

	public void setPWMFreq(double freq) throws InterruptedException, IOException
	{
		// "Sets the PWM frequency"
		double prescaleval = 25000000.0;// # 25MHz
		prescaleval /= 4096.0; // # 12-bit
		prescaleval /= freq;
		prescaleval -= 1.0;

		System.out.println("Setting PWM frequency to " + freq + " Hz");
		System.out.println("Estimated pre-scale: " + prescaleval);
		int prescale =(int) (prescaleval + 0.5);

		System.out.println("Final pre-scale: " + prescale);

		byte oldmode = (byte) device.read(MODE1);
		byte newmode = (byte) ((oldmode & 0x7F) | 0x10);// # sleep
		device.write(MODE1, newmode);// # go to sleep
		device.write(PRESCALE, (byte) (prescale));
		device.write(MODE1, oldmode);
		Thread.sleep(1);
		device.write(MODE1, (byte) (oldmode | 0x80));

	}

	public void setPwm(int channel, int on, int off) throws IOException
	{

		// "Sets a single PWM channel"
		int offset = 4 * channel;
		device.write(LED0_ON_L + offset, (byte) (on & 0xFF));
		device.write(LED0_ON_H + offset, (byte) (on >> 8));
		device.write(LED0_OFF_L + offset, (byte) (off & 0xFF));
		device.write(LED0_OFF_H + offset, (byte) (off >> 8));
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public void export(Pin pin, PinMode mode)
	{
		if (mode != PinMode.PWM_OUTPUT)
			throw new UnsupportedPinModeException(pin, mode);
		// make sure to set the pin mode
		super.export(pin, mode);
		setMode(pin, mode);
	}

	@Override
	public void unexport(Pin pin)
	{
		super.unexport(pin);
		setMode(pin, PinMode.PWM_OUTPUT);
	}

	@Override
	public void setMode(Pin pin, PinMode mode)
	{
		if (mode != PinMode.PWM_OUTPUT)
			throw new UnsupportedPinModeException(pin, mode);

		// cache mode
		getPinCache(pin).setMode(mode);

	}

	@Override
	public PinMode getMode(Pin pin)
	{
		return super.getMode(pin);
	}

	@Override
	public void setValue(Pin pin, double value)
	{ // validate
		if (hasPin(pin) == false)
			throw new InvalidPinException(pin);

		throw new UnsupportedOperationException("setValue not supported");
	}

	@Override
	public double getValue(Pin pin)
	{ // validate
		if (hasPin(pin) == false)
			throw new InvalidPinException(pin);

		throw new UnsupportedOperationException("getValue not supported");
	}

	@Override
	public void setPwm(Pin pin, int value)
	{
		// validate
		if (hasPin(pin) == false)
			throw new InvalidPinException(pin);

		// determine pin address
	//	System.out.println("address: " + pin.getAddress());
		int pinAddress = pin.getAddress();

		try
		{
			setPwm(pinAddress, 0, value);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// cache pin state
		getPinCache(pin).setPwmValue(value);

	}

	@Override
	public void setState(Pin pin, PinState state)
	{
		// validate
		if (hasPin(pin) == false)
			throw new InvalidPinException(pin);

		throw new UnsupportedOperationException("setState not supported");
	}

	@Override
	public void shutdown()
	{
		try
		{

			// close the I2C bus communication
			bus.close();
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

}
