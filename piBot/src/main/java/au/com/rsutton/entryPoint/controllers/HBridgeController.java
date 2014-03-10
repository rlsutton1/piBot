package au.com.rsutton.entryPoint.controllers;

import com.pi4j.gpio.extension.adafruit.PwmPin;

public class HBridgeController
{

	private PwmPin pwmPin;

	private double direction = 1;

	private int pwmRange = 4095;

	private PwmPin directionPin;

	public HBridgeController(PwmPin pwmPin, PwmPin directionPin,
			boolean invertDirection)
	{
		this.pwmPin = pwmPin;
		this.directionPin = directionPin;
		if (invertDirection)
		{
			direction = direction * -1;
		}
		pwmPin.setPwmValue(0);
	}

	public void setOutput(double percent)
	{
		if (percent > 1 || percent < -1)
		{
			System.out.println("Rejected speed "+percent);
			throw new RuntimeException("Cant set HBridge to -1<x>1");
		}

		int pwm = (int) (pwmRange * (Math.abs(percent)));
		int dir = 0;
		if ((percent * direction) > 0)
		{
			dir = 4095;
		}
		directionPin.setPwmValue(dir);
		pwmPin.setPwmValue(pwm);
		// System.out.println("Setting HBridge to PWM:" + pwm + ", DIR:" + dir
		// + " for percentage " + percent + " pin " + pwmPin.toString());

	}

	public void setDirection(int direction)
	{
		this.direction = direction;

	}

	public int getDirection()
	{

		return (int) direction;
	}

}
