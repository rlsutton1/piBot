package com.pi4j.gpio.extension.adafruit;

import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;

public class PwmPin
{

	private Pin pin;
	private GpioProvider provider;

	public PwmPin(GpioProvider provider, Pin pin)
	{
		this.pin = pin;
		this.provider = provider;
		provider.export(pin, PinMode.PWM_OUTPUT);

	}

	/**
	 * range 0 - 4095;
	 * 
	 * @param value
	 */
	public void setPwmValue(int value)
	{
		provider.setPwm(pin, value);
	}

	@Override
	public String toString()
	{
		return pin.getName();
	}

}
