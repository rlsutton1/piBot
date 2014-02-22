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

	Speed desiredSpeed;
	TimeUnit timeUnit = TimeUnit.SECONDS;
	DistanceUnit distUnit = DistanceUnit.MM;
	private WheelController leftWheel;
	private WheelController rightWheel;
	private int desiredHeading;
	private int actualHeading;

	public SpeedHeadingController(WheelController leftWheel,
			WheelController rightWheel) throws IOException,
			InterruptedException
	{
		this.leftWheel = leftWheel;
		this.rightWheel = rightWheel;
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

		double changeInHeading = HeadingHelper.getChangeInHeading(
				actualHeading, desiredHeading);

		if (desiredSpeed.getSpeed(DistanceUnit.MM, TimeUnit.SECONDS) > 4
				|| desiredSpeed.getSpeed(DistanceUnit.MM, TimeUnit.SECONDS) < -4
				|| Math.abs(changeInHeading) < 5)
		{
			// we're either moving or our heading is off by more than 1 degree
			if (pid == null)
			{
				pid = new Pid(1, .10, .1, 100, 100, -100, true);
			}
			double offset = pid.computePid(0, changeInHeading);
			double speed = desiredSpeed.getSpeed(distUnit, timeUnit);
			double left = speed * ((100 + offset) / 100.0d);
			double right = speed * ((100 - offset) / 100.0d);
			Speed leftSpeed = new Speed(new Distance(left, distUnit),
					Time.perSecond());
			Speed rightSpeed = new Speed(new Distance(right, distUnit),
					Time.perSecond());

			leftWheel.setSpeed(leftSpeed);
			rightWheel.setSpeed(rightSpeed);

		} else
		{
			// we're stationary and pointing approximately the right direction,
			// so stop;
			Speed speed = new Speed(new Distance(0, distUnit), Time.perSecond());
			leftWheel.setSpeed(speed);
			rightWheel.setSpeed(speed);
			pid = null;
		}
	}

}
