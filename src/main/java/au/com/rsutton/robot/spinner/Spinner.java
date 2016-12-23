package au.com.rsutton.robot.spinner;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.pi4j.gpio.extension.grovePi.GrovePiPin;
import com.pi4j.gpio.extension.grovePi.GrovePiProvider;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import au.com.rsutton.config.Config;

public class Spinner implements Runnable
{

	private static final long ONE_SECOND_IN_NANOS = TimeUnit.SECONDS.toNanos(1);
	GpioPinDigitalOutput stepPin;
	GpioPinDigitalOutput dirPin;

	Stopwatch lastStep = Stopwatch.createStarted();

	private int lastDirection = 0;
	private long timeBetweenSteps;

	private GrovePiProvider grove;

	static final double stepsPerRotation = 200;

	static final double microSteps = 8;

	boolean stop = false;

	public Spinner(long stepsPerSecond, GrovePiProvider grove, Config config)
			throws InterruptedException, IOException, UnsupportedBusNumberException
	{

		timeBetweenSteps = ONE_SECOND_IN_NANOS / stepsPerSecond;
		final GpioController gpio = GpioFactory.getInstance();

		// provision gpio pin #01 as an output pin and turn on
		stepPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, "StepPin", PinState.LOW);
		dirPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, "DirPin", PinState.LOW);

		// set shutdown state for this pin
		stepPin.setShutdownOptions(true, PinState.LOW);
		dirPin.setShutdownOptions(true, PinState.LOW);

		this.grove = grove;

		grove.setMode(GrovePiPin.GPIO_A2, PinMode.ANALOG_INPUT);

		double ldrMax = 0;

		int maxPos = 0;

		for (int i = 0; i < stepsPerRotation * microSteps; i += microSteps)
		{
			moveTo(i);
			double value = getLdrValue();
			System.out.println(value);

			if (value > ldrMax)
			{
				maxPos = i;
			}
			ldrMax = Math.max(ldrMax, value);

		}

		moveTo((long) (maxPos + (0 * microSteps)));
		setZero();

		new Thread(this, "Spinner").start();
		new Lidar(this, config);

	}

	private double getLdrValue()
	{
		int samples = 5;
		double v = 0;
		for (int i = 0; i < samples; i++)
		{
			v += grove.getValue(GrovePiPin.GPIO_A2);
		}
		return v / samples;
	}

	private void step(int direction) throws InterruptedException
	{

		if ((int) Math.signum(direction) != lastDirection)
		{
			if ((int) Math.signum(direction) > 0)
			{
				dirPin.setState(PinState.HIGH);
			} else
			{
				dirPin.setState(PinState.LOW);
			}
		}
		// if (lastStep.elapsed(TimeUnit.MILLISECONDS) > 5 &&
		// lastStep.elapsed(TimeUnit.MILLISECONDS) < 26)
		// {
		// Thread.sleep(30 - lastStep.elapsed(TimeUnit.MILLISECONDS));
		// }

		currentPosition += Math.signum(direction);

		checkIfDelayIsRequired(direction);

		// step time will most likely be more than 500 nanos
		// busyWaitNannos(500);

		stepPin.setState(PinState.HIGH);
		busyWaitNannos(200);
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
		long elapsed = lastStep.elapsed(TimeUnit.NANOSECONDS);
		if (elapsed < TimeUnit.MILLISECONDS.toNanos(30))
		{
			if (((int) Math.signum(direction)) != lastDirection)
			{

				// minimun 30ms for change of direction
				Thread.sleep(30 - TimeUnit.NANOSECONDS.toMillis(elapsed));
			} else if (elapsed < timeBetweenSteps)
			{
				busyWaitNannos(timeBetweenSteps - elapsed);
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

	public long getCurrentPosition()
	{
		return (long) (currentPosition % (microSteps * stepsPerRotation));
	}

	public void setStepSpeed(long stepsPerSecond)
	{
		timeBetweenSteps = ONE_SECOND_IN_NANOS / stepsPerSecond;

	}

	@Override
	public void run()
	{
		long pos = 0;
		while (!stop)
		{
			pos += (microSteps * stepsPerRotation);
			try
			{
				moveTo(pos);
				Thread.sleep(50);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

	}

	public boolean isValidPosition()
	{
		long totalSteps = (long) (microSteps * stepsPerRotation);
		long pos = currentPosition % totalSteps;
		return pos > (totalSteps * 0.15) && pos < (totalSteps * 0.85);

	}

}
