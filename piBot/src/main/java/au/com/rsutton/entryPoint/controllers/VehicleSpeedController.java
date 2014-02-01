package au.com.rsutton.entryPoint.controllers;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;

public class VehicleSpeedController implements Runnable
{

	private HBridgeController left;
	private HBridgeController right;
	volatile private double setSpeed;
	volatile private double directionAdjustment;
	volatile private double actualSpeed;
	private final static DistanceUnit dUnit = DistanceUnit.MM;
	private final static TimeUnit tUnit = TimeUnit.SECONDS;
	private Pid pid;

	public VehicleSpeedController(HBridgeController left,
			HBridgeController right)
	{
		this.left = left;
		this.right = right;

		pid = new Pid(.4, .2, .1, 100, 100, -100, false);

		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this,
				100, 100, TimeUnit.MILLISECONDS);

	}

	public void stop()
	{
		setSpeed = 0;
		actualSpeed = 0.0d;

	}

	/**
	 * -100 < speedPercent < 100
	 * 
	 * @param speedPercent
	 */
	public void setSpeed(Speed speedPercent)
	{
		setSpeed = speedPercent.getSpeed(dUnit, tUnit);
		System.out.println("Speed set to " + setSpeed);

	}

	private void setServoSpeed(HBridgeController servo, double rightSpeed)
	{
		servo.setOutput((rightSpeed / 100.0));
	}

	/**
	 * -100 < d < 100
	 * 
	 * @param d
	 */
	public void setDirectionAdjustment(double d)
	{
		directionAdjustment = d;
	}

	@Override
	public void run()
	{
		try
		{

			double power = pid.computePid(setSpeed, actualSpeed);
			if (actualSpeed > 0.1 || actualSpeed < -0.1 || power > 30
					|| power < -30)
			{
				System.out.println("set:" + setSpeed + " actual:" + actualSpeed
						+ " power:" + power);
			}

			double leftPower = power + directionAdjustment;
			double rightPower = power - directionAdjustment;

			double adjustment = 0;
			if (Math.max(leftPower, rightPower) > 100)
			{
				adjustment = 100 - Math.max(leftPower, rightPower);
			} else if (Math.min(leftPower, rightPower) < -100)
			{
				adjustment = -100 - Math.min(leftPower, rightPower);
			}
			leftPower += adjustment;
			rightPower += adjustment;
			// System.out.println("leftSpeed " + leftPower + " rightSpeed "
			// + rightPower);

			setServoSpeed(left, leftPower);
			setServoSpeed(right, rightPower);
		} catch (Throwable e)
		{
			e.printStackTrace();
		}

	}

	public Speed getSetSpeed()
	{
		return new Speed(new Distance(setSpeed, dUnit), new Time(1, tUnit));
	}

	public void setActualSpeed(Speed speed)
	{
		actualSpeed = speed.getSpeed(dUnit, tUnit);

	}

}
