package au.com.rsutton.entryPoint.controllers;

import com.pi4j.gpio.extension.adafruit.PwmPin;

public class ServoController
{

	private PwmPin pin;

	public static final int NORMAL = 1;
	public static final int REVERSED = -1;

	private boolean useCenterStop = false;
	private int max;
	private int min;
	private int direction;

	public ServoController(PwmPin pin, int min, int max, int direction)
	{
		this.max = max;
		this.min = min;
		this.pin = pin;
		this.direction = direction;
		pin.setPwmValue(0);
	}

	public void setOutput(double percent)
	{
		if (percent > 100 || percent < -100)
		{
			throw new RuntimeException("Cant set servo to -100<x>100");
		}
		int position = getActualPwmOutput(percent);

		System.out.println("Setting servo to " + position + " for percentage "
				+ percent);

		// lastSetPercent = (int) percent;
		pin.setPwmValue(position);
	}

	public void setUseCenterStop(boolean b)
	{
		useCenterStop = b;
	}

	public void turnOff()
	{
		pin.setPwmValue(0);
	}

	public int getActualPwmOutput(double percent)
	{

		percent *= direction;
		// we assume at 0, the servo is actually off.
		percent += 100;
		if (percent < 0)
			percent = 1;
		if (percent > 200)
			percent = 200;

		int position = 0;

		position = (int) (((max - min) * (percent / 200.0)) + min);
		if (position < 1)
			position = 1;
		return position;
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
