package au.com.rsutton.entryPoint.controllers;

import com.pi4j.gpio.extension.adafruit.DigitalOutPin;
import com.pi4j.gpio.extension.adafruit.PwmPin;

public class HBridgeController
{

	private PwmPin pwmPin;

	private double direction = 1;

	private int pwmRange = 255;

	private DigitalOutPin directionPin;

	private DeadZoneRescaler deadZoneRescaler;

	/**
	 * 
	 * @param pwmPin
	 * @param directionPin
	 * @param invertDirection
	 * @param deadZoneFraction
	 *            value between 0 and 1 indicating the power range between 0 and
	 *            deadZoneFraction where the motors mechanical resistance is to
	 *            great to result in movement
	 */
	public HBridgeController(PwmPin pwmPin, DigitalOutPin directionPin,
			boolean invertDirection, double deadZoneFraction)
	{
		this.deadZoneRescaler = new DeadZoneRescaler(pwmRange,
				(int) (deadZoneFraction * pwmRange));
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
			System.out.println("Rejected speed " + percent);
			throw new RuntimeException("Cant set HBridge to -1<x>1");
		}

		int pwm = (int) (pwmRange * (Math.abs(percent)));

		boolean dir = (percent * direction) > 0;
		if (dir)
		{
			directionPin.setHigh();
		} else
		{
			directionPin.setLow();
		}

		pwmPin.setPwmValue(deadZoneRescaler.rescale(pwm));
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
