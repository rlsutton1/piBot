package au.com.rsutton.robot.stepper;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class StepperMotor
{

	GpioPinDigitalOutput stepPin;
	GpioPinDigitalOutput dirPin;

	int stepDelay = 5;
	Stopwatch lastStep = Stopwatch.createStarted();

	private int lastDirection;

	int microSteps = 8;

	public StepperMotor(int microSteps)
	{

		this.microSteps = microSteps;
		final GpioController gpio = GpioFactory.getInstance();

		// provision gpio pin #01 as an output pin and turn on
		stepPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, "StepPin", PinState.LOW);
		dirPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, "DirPin", PinState.LOW);

		// set shutdown state for this pin
		stepPin.setShutdownOptions(true, PinState.LOW);
		dirPin.setShutdownOptions(true, PinState.LOW);

	}

	List<Long> timeOfStep = new LinkedList<>();

	private void step(int direction) throws InterruptedException
	{
		checkIfDelayIsRequired(direction);

		// log the time of this step
		timeOfStep.add(System.currentTimeMillis());
		if (timeOfStep.size() > microSteps)
		{
			// trim the list of steps
			timeOfStep.remove(0);
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
		busyWaitNannos(500);

		stepPin.setState(PinState.HIGH);
		busyWaitNannos(500);
		stepPin.setState(PinState.LOW);

		lastDirection = (int) Math.signum(direction);
		lastStep.reset();
		lastStep.start();
	}

	/**
	 * we need a large delay if changing direction.
	 * 
	 * we need no delay when micro stepping
	 * 
	 * we need a small day when moving a full step
	 * 
	 * @param direction
	 * @throws InterruptedException
	 */
	private void checkIfDelayIsRequired(int direction) throws InterruptedException
	{
		long elapsed = lastStep.elapsed(TimeUnit.MILLISECONDS);
		if (elapsed < 30)
		{
			if (((int) Math.signum(direction)) != lastDirection)
			{
				timeOfStep.clear();
				// minimun 20ms for change of direction
				Thread.sleep(30 - elapsed);
			} else if (elapsed < stepDelay)
			{
				if (timeOfStep.size() >= microSteps)
				{
					// we've exceeded one full step

					// get the elapsed time since the last full step
					Long oldestStep = timeOfStep.get(0);
					elapsed = Math.abs(System.currentTimeMillis() - oldestStep);
				}
				long sleepTime = stepDelay - elapsed;
				if (sleepTime > 0)
				{
					// minimum 5ms step to step
					Thread.sleep(stepDelay - elapsed);
				}
			}

		}
	}

	private void busyWaitNannos(long duration)
	{
		// busy wait duration nanos
		long start = System.nanoTime();
		while (Math.abs(start - System.nanoTime()) < duration)
			;
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
