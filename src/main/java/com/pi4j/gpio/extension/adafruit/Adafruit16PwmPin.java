package com.pi4j.gpio.extension.adafruit;

import java.util.EnumSet;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.impl.PinImpl;

/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: GPIO Extension
 * FILENAME      :  MCP23008Pin.java  
 * 
 * This file is part of the Pi4J project. More information about 
 * this project can be found here:  http://www.pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 Pi4J
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License
 * at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
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
public class Adafruit16PwmPin
{
	public static final Pin GPIO_00 = createDigitalPin(0, "GPIO 0");
	public static final Pin GPIO_01 = createDigitalPin(1, "GPIO 1");
	public static final Pin GPIO_02 = createDigitalPin(2, "GPIO 2");
	public static final Pin GPIO_03 = createDigitalPin(3, "GPIO 3");
	public static final Pin GPIO_04 = createDigitalPin(4, "GPIO 4");
	public static final Pin GPIO_05 = createDigitalPin(5, "GPIO 5");
	public static final Pin GPIO_06 = createDigitalPin(6, "GPIO 6");
	public static final Pin GPIO_07 = createDigitalPin(7, "GPIO 7");
	public static final Pin GPIO_08 = createDigitalPin(8, "GPIO 8");
	public static final Pin GPIO_09 = createDigitalPin(9, "GPIO 9");
	public static final Pin GPIO_10 = createDigitalPin(10, "GPIO 10");
	public static final Pin GPIO_11 = createDigitalPin(11, "GPIO 11");
	public static final Pin GPIO_12 = createDigitalPin(12, "GPIO 12");
	public static final Pin GPIO_13 = createDigitalPin(13, "GPIO 13");
	public static final Pin GPIO_14 = createDigitalPin(14, "GPIO 14");
	public static final Pin GPIO_15 = createDigitalPin(15, "GPIO 15");

	public static Pin[] ALL = {
			Adafruit16PwmPin.GPIO_00, Adafruit16PwmPin.GPIO_01,
			Adafruit16PwmPin.GPIO_02, Adafruit16PwmPin.GPIO_03,
			Adafruit16PwmPin.GPIO_04, Adafruit16PwmPin.GPIO_05,
			Adafruit16PwmPin.GPIO_06, Adafruit16PwmPin.GPIO_07,
			Adafruit16PwmPin.GPIO_08, Adafruit16PwmPin.GPIO_09,
			Adafruit16PwmPin.GPIO_10, Adafruit16PwmPin.GPIO_11,
			Adafruit16PwmPin.GPIO_12, Adafruit16PwmPin.GPIO_13,
			Adafruit16PwmPin.GPIO_14, Adafruit16PwmPin.GPIO_15 };

	private static Pin createDigitalPin(int address, String name)
	{
		return new PinImpl(Adafruit16PwmProvider.NAME, address, name,
				EnumSet.of(PinMode.PWM_OUTPUT), null);
	}
}
