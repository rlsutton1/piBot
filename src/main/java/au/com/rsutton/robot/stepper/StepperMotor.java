package au.com.rsutton.robot.stepper;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.pi4j.gpio.extension.adafruit.AdafruitPCA9685;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class StepperMotor
{

	int steps[] = new int[] {
			0, 3, 1, 2 };

	int previousStepPosition = 0;

	int stepPosition = 0;

	GpioPinDigitalOutput stepPin;
	GpioPinDigitalOutput dirPin;

	int stepDelay = 6;
	Stopwatch lastStep = Stopwatch.createStarted();

	private int lastDirection;

	public StepperMotor()
	{

		final GpioController gpio = GpioFactory.getInstance();

		// provision gpio pin #01 as an output pin and turn on
		stepPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, "StepPin",
				PinState.LOW);
		dirPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, "DirPin",
				PinState.LOW);

		// set shutdown state for this pin
		stepPin.setShutdownOptions(true, PinState.LOW);
		dirPin.setShutdownOptions(true, PinState.LOW);

	}

	private void step(int direction) throws InterruptedException
	{
		long elapsed = lastStep.elapsed(TimeUnit.MILLISECONDS);
		if (elapsed < 30)
		{
			if (((int) Math.signum(direction)) != lastDirection)
			{
				// minimun 20ms for change of direction
				Thread.sleep(30 - elapsed);
			} else if (elapsed < stepDelay)
			{
				// minimum 5ms step to step
				Thread.sleep(stepDelay - elapsed);
			}

		}

		if (direction > 0)
		{
			dirPin.setState(PinState.HIGH);
			currentPosition++;
		} else
		{
			dirPin.setState(PinState.LOW);
			currentPosition--;
		}

		for (int t = 0; t < 8; t++)
		{
			stepPin.setState(PinState.HIGH);
			Thread.sleep(1);
			stepPin.setState(PinState.LOW);
			Thread.sleep(1);
		}
		lastDirection = (int) Math.signum(direction);
		lastStep.reset();
		lastStep.start();
	}

	public void moveTo(long position) throws InterruptedException
	{
		requiredPosition = position;
		while (currentPosition != requiredPosition)
		{
			step((int) (requiredPosition - currentPosition));
		}

	}

	long currentPosition;
	long requiredPosition;

	public void setZero()
	{
		currentPosition = 0;

	}

	public void setStepSpeed(int s)
	{
		stepDelay = s;

	}

}
