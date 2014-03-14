package au.com.rsutton.robot.rover;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
	private int actualHeading;

	public SpeedHeadingController(WheelController leftWheel,
			WheelController rightWheel, float intialHeading)
			throws IOException, InterruptedException
	{
		this.leftWheel = leftWheel;
		this.rightWheel = rightWheel;
		desiredHeading = (int) intialHeading;
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this,
				100, 100, TimeUnit.MILLISECONDS);
	}

	public void setActualHeading(int heading)
	{
		actualHeading = heading;
	}

	public void setDesiredMotion(SetMotion motion)
	{
		desiredHeading = motion.getHeading().intValue();
		desiredSpeed = motion.getSpeed();
	}

	@Override
	public void run()
	{

		try
		{
			double changeInHeading = HeadingHelper.getChangeInHeading(
					actualHeading, desiredHeading);

			if (Math.abs(desiredSpeed.getSpeed(DistanceUnit.MM,
					TimeUnit.SECONDS)) > 4 || Math.abs(changeInHeading) > 2)
			{
				// we're either moving or our heading is off by more than 1
				// degree
				if (pid == null)
				{
					//System.out.println("new heading pid");
					pid = new Pid(4, .10, .1, 100, 99, -99, true);
				}
				double offset = pid.computePid(0, changeInHeading);
				// System.out.println("o " + offset + " c" + changeInHeading
				// + " d " + desiredHeading);
				double speed = desiredSpeed.getSpeed(distUnit, timeUnit);

				

				double left = speed * ((100.0d + offset) / 100.0d);
				double right = speed * ((100.0d - offset) / 100.0d);

				if (Math.abs(left) < Math.abs(offset)
						&& Math.abs(right) < Math.abs(offset))
				{
					// we are almost stationary so scaling the speed doesn't
					// work.
					left = speed + offset;
					right = speed - offset;
				}

				Speed leftSpeed = new Speed(new Distance(left, distUnit),
						Time.perSecond());
				Speed rightSpeed = new Speed(new Distance(right, distUnit),
						Time.perSecond());

				leftWheel.setSpeed(leftSpeed);
				rightWheel.setSpeed(rightSpeed);

			} else
			{
				// System.out.println("Stationary DesiredSpeed:" +
				// desiredSpeed);
				// we're stationary and pointing approximately the right
				// direction,
				// so stop;
				Speed speed = new Speed(new Distance(0, distUnit),
						Time.perSecond());
				leftWheel.setSpeed(speed);
				rightWheel.setSpeed(speed);
				pid = null;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private double rangeLimit(double value, int lowerLimit, int upperLimit)
	{
		return Math.max(lowerLimit, Math.min(value, upperLimit));
	}

}
