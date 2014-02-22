package au.com.rsutton.robot.rover;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import au.com.rsutton.entryPoint.controllers.HBridgeController;
import au.com.rsutton.entryPoint.controllers.Pid;
import au.com.rsutton.entryPoint.quadrature.QuadratureEncoding;
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
	final private QuadratureEncoding quadrature;
	private volatile double setSpeed;
	private volatile int lastQuadratureOffset;
	private volatile long lastCalcuationTime = System.currentTimeMillis();
	private volatile Pid pid;
	final QuadratureToDistance distanceConverter = new QuadratureToDistance();
	final private DistanceUnit distUnit = DistanceUnit.MM;
	final private TimeUnit timeUnit = TimeUnit.SECONDS;

	WheelController(PwmPin pwmPin, PwmPin directionPin, Pin quadratureA,
			Pin quadreatureB,boolean invertPwm,boolean invertQuadrature)
	{

		hBridge = new HBridgeController(pwmPin, directionPin,
				invertPwm);

		hBridge.setOutput(0);

		quadrature = new QuadratureEncoding(quadratureA, quadreatureB, invertQuadrature);
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this,
				100, 100, TimeUnit.MILLISECONDS);


	}

	/**
	 * set the speed that this wheel should travel at
	 * @param speed
	 */
	public void setSpeed(Speed speed)
	{
		setSpeed = speed.getSpeed(distUnit, timeUnit);
	}

	/**
	 * get the distance covered by this wheel
	 * @return
	 */
	public Distance getDistance()
	{
		return distanceConverter.scale((int) quadrature.getValue());

	}

	@Override
	public void run()
	{
		try
		{
			double actualSpeed = getActualSpeed();

			if (setSpeed < 10 && (actualSpeed < 10 && actualSpeed > -10))
			{
				if (pid != null)
				{
					System.out.println("Stopping speed controller pid");
					pid = null;
				}

			} else if (pid == null)
			{
				System.out.println("creating new speed controller pid");
				pid = new Pid(.1, .01, .01, 100, 100, -100, false);
			}
			double power = 0;
			if (pid != null)
			{
				power = pid.computePid(setSpeed, actualSpeed);
			}

			hBridge.setOutput(power / 100.0d);
		} catch (Throwable e)
		{
			e.printStackTrace();
		}

	}

	private double getActualSpeed()
	{
		double change = (int) (quadrature.getValue() - lastQuadratureOffset);
		long now = System.currentTimeMillis();

		long elapsed = now - lastCalcuationTime;
		lastCalcuationTime = now;

		change = change / (((double) elapsed) / 1000.0d);

		Distance distance = distanceConverter.scale((int) change);

		return new Speed(distance, Time.perSecond()).getSpeed(distUnit,
				timeUnit);

	}
}
