package au.com.rsutton.robot.rover;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.pi4j.gpio.extension.lsm303.HeadingData;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.entryPoint.controllers.Pid;
import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;
import au.com.rsutton.hazelcast.SetMotion;

public class SpeedHeadingController implements Runnable
{

	private Pid pid;
	TimeUnit timeUnit = TimeUnit.SECONDS;
	DistanceUnit distUnit = DistanceUnit.MM;

	Speed desiredSpeed = new Speed(new Distance(0, distUnit), Time.perSecond());
	private WheelController leftWheel;
	private WheelController rightWheel;
	private int desiredHeading;
	private HeadingData actualHeading;
	volatile boolean freeze = false;

	public SpeedHeadingController(WheelController leftWheel, WheelController rightWheel, float intialHeading)
			throws IOException, InterruptedException
	{
		this.leftWheel = leftWheel;
		this.rightWheel = rightWheel;
		desiredHeading = (int) intialHeading;
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this, 100, 100, TimeUnit.MILLISECONDS);
	}

	public void setActualHeading(HeadingData heading)
	{
		actualHeading = heading;
	}

	public void setDesiredMotion(SetMotion motion)
	{
		desiredHeading = motion.getHeading().intValue();
		desiredSpeed = motion.getSpeed();
		freeze = motion.getFreeze();
	}

	@Override
	public void run()
	{

		try
		{
			double changeInHeading = HeadingHelper.getChangeInHeading(actualHeading.getHeading(), desiredHeading);

			if (!freeze && isMovementRequired(changeInHeading) && isErrorSmallEnough())
			{
				// we're either moving or our heading is off by more than 1
				// degree
				if (pid == null)
				{
					// System.out.println("new heading pid");
					pid = new Pid(4, .10, .1, 100, 99, -99, true);
				}
				double offset = pid.computePid(0, changeInHeading);
				// System.out.println("o " + offset + " c" + changeInHeading
				// + " d " + desiredHeading);
				double speed = desiredSpeed.getSpeed(distUnit, timeUnit);

				double portionOfSpeed = (offset / 100d) * speed;

				double inversion = Math.signum(speed);

				double left = speed + (portionOfSpeed * inversion);
				double right = speed - (portionOfSpeed * inversion);

				if (Math.abs(left) < Math.abs(offset / 10d) && Math.abs(right) < Math.abs(offset / 10d))
				{
					// we are almost stationary so scaling the speed doesn't
					// work.
					left = speed + (offset * 1d);
					right = speed - (offset * 1d);
				}

				Speed leftSpeed = new Speed(new Distance(left, distUnit), Time.perSecond());
				Speed rightSpeed = new Speed(new Distance(right, distUnit), Time.perSecond());

				leftWheel.setSpeed(leftSpeed);
				rightWheel.setSpeed(rightSpeed);

			} else
			{
				// System.out.println("Stationary DesiredSpeed:" +
				// desiredSpeed);
				// we're stationary and pointing approximately the right
				// direction,
				// so stop;
				Speed speed = new Speed(new Distance(0, distUnit), Time.perSecond());
				leftWheel.setSpeed(speed);
				rightWheel.setSpeed(speed);
				pid = null;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private boolean isMovementRequired(double changeInHeading)
	{
		return Math.abs(desiredSpeed.getSpeed(DistanceUnit.MM, TimeUnit.SECONDS)) > 4 || Math.abs(changeInHeading) > 2;
	}

	private boolean isErrorSmallEnough()
	{
		double maxError = 8;
		boolean ret = actualHeading.getError() < maxError;
		if (!ret)
		{
			System.out.println("Heading Error is to large, stopping until error is less than " + maxError
					+ " current Error is " + actualHeading.getError());
		}
		return ret;
	}

}
