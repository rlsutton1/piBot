package au.com.rsutton.hazelcast;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;
import au.com.rsutton.mapping.particleFilter.RobotPoseSource;

public class TargetLocation
{

	private final static DistanceUnit unit = DistanceUnit.MM;

	private Distance accuracy = new Distance(10, DistanceUnit.CM);
	RobotPoseSource poseSource;

	public void gotoTarget(Distance targetXd, Distance targetYd, RobotPoseSource poseSource)
			throws InstantiationException, IllegalAccessException, InterruptedException
	{
		this.poseSource = poseSource;
		double targetX = targetXd.convert(unit);
		double targetY = targetYd.convert(unit);
		SetMotion message = new SetMotion();

		double distance = 1000d;
		while (distance > accuracy.convert(unit))
		{

			double x = poseSource.getXyPosition().getX().convert(unit);
			double y = poseSource.getXyPosition().getY().convert(unit);
			double heading = poseSource.getHeading();
			double newHeading = Math.toDegrees(Math.atan2((targetX - x), (targetY - y)));

			// pythag to workout the distance to the location
			distance = Math.abs(Math.sqrt(Math.pow(targetX - x, 2) + Math.pow(targetY - y, 2)));

			// speed is a product of accurace of desired heading and distance to
			// the target
			double changeInHeading = HeadingHelper.getChangeInHeading(newHeading, heading);
			message.setChangeHeading(changeInHeading);
			double speed = 400;

			// there is a lot of slip if we turn while on the move
			if (changeInHeading > 5)
			{
				speed = 0;
			}
			speed = Math.min(distance / 1, speed);

			message.setSpeed(new Speed(new Distance(speed, DistanceUnit.MM), Time.perSecond()));
			message.publish();
			Thread.sleep(250);
		}

	}

}