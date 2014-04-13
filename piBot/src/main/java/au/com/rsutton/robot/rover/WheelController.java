package au.com.rsutton.robot.rover;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import au.com.rsutton.entryPoint.controllers.HBridgeController;
import au.com.rsutton.entryPoint.controllers.Pid;
import au.com.rsutton.entryPoint.quadrature.QuadratureEncoding;
import au.com.rsutton.entryPoint.quadrature.QuadratureEncodingCBridge;
import au.com.rsutton.entryPoint.quadrature.QuadratureProvider;
import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;
import au.com.rsutton.robot.QuadratureToDistance;

import com.pi4j.gpio.extension.adafruit.PwmPin;
import com.pi4j.io.gpio.Pin;

public class WheelController implements Runnable
{

	final private HBridgeController hBridge;
	final private QuadratureProvider quadrature;
	private volatile double setSpeed;
	private volatile long lastQuadratureOffset;
	private volatile long lastCalcuationTime = System.currentTimeMillis();
	private volatile Pid pid;
	final QuadratureToDistance distanceConverter = new QuadratureToDistance();
	final private DistanceUnit distUnit = DistanceUnit.MM;
	final private TimeUnit timeUnit = TimeUnit.SECONDS;
	final private ScheduledFuture<?> worker;

	WheelController(PwmPin pwmPin, PwmPin directionPin, Pin quadratureA,
			Pin quadreatureB, boolean invertPwm, boolean invertQuadrature) throws IOException
	{

		hBridge = new HBridgeController(pwmPin, directionPin, invertPwm);

		hBridge.setOutput(0);

		quadrature = new QuadratureEncodingCBridge(quadratureA, quadreatureB,
				invertQuadrature);
		worker = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this,
				100, 100, TimeUnit.MILLISECONDS);

	}

	/**
	 * set the speed that this wheel should travel at
	 * 
	 * @param speed
	 */
	public void setSpeed(Speed speed)
	{
		setSpeed = speed.getSpeed(distUnit, timeUnit);
	}

	/**
	 * get the distance covered by this wheel
	 * 
	 * @return
	 */
	public Distance getDistance()
	{
		try
		{
			return distanceConverter.scale((int) quadrature.getValue());
		} catch (IOException e)
		{
			worker.cancel(false);
			e.printStackTrace();
			hBridge.setOutput(0);

		}
		return new Distance(0, DistanceUnit.MM);

	}

	@Override
	public void run()
	{
		try
		{
			double actualSpeed = getActualSpeed();

			if (Math.abs(setSpeed) < 10 && Math.abs(actualSpeed) < 10)
			{
				if (pid != null)
				{
					// System.out.println("Stopping speed controller pid");
					pid = null;
				}

			} else if (pid == null)
			{
				// System.out.println("creating new speed controller pid");
				pid = new Pid(.25, .05, .01, 100, 99, -99, false);
			}
			double power = 0;
			if (pid != null)
			{
				power = pid.computePid(setSpeed, actualSpeed);
			}

			// System.out.println("A " + actualSpeed + " S " + setSpeed + " P "
			// + power);

			hBridge.setOutput(power / 100.0d);
		} catch (Throwable e)
		{
			e.printStackTrace();
		}

	}

	private double getActualSpeed()
	{
		long currentQuadrature=0;
		try
		{
			currentQuadrature = quadrature.getValue();
		} catch (IOException e)
		{
			worker.cancel(false);
			hBridge.setOutput(0);
			e.printStackTrace();
		}
		double change = (int) (currentQuadrature - lastQuadratureOffset);
		long now = System.currentTimeMillis();

		long elapsed = now - lastCalcuationTime;
		if (Math.abs(change) > 0)
		{
			lastCalcuationTime = now;
			lastQuadratureOffset = currentQuadrature;
		}

		change = change / (((double) elapsed) / 1000.0d);

		Distance distance = distanceConverter.scale((int) change);

		return new Speed(distance, Time.perSecond()).getSpeed(distUnit,
				timeUnit);

	}
}
