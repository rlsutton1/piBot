package com.pi4j.gpio.extension.adafruit;

import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;

public class DigitalOutPin
{

	private Pin pin;
	private GpioProvider provider;

	public DigitalOutPin(GpioProvider provider, Pin pin)
	{
		this.pin = pin;
		this.provider = provider;
		provider.export(pin, PinMode.DIGITAL_OUTPUT);

	}

	public void setHigh()
	{
		provider.setValue(pin, 1);
	}

	public void setLow()
	{
		provider.setValue(pin, 0);
	}

	
	@Override
	public String toString()
	{
		return pin.getName();
	}

}
