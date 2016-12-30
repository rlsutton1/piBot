package au.com.rsutton.hazelcast;

import java.util.Collection;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;
import au.com.rsutton.mapping.XY;
import au.com.rsutton.robot.rover.LidarObservation;
import au.com.rsutton.robot.rover.MovingLidarObservationMultiBuffer;

public class WallFollower implements Runnable, MessageListener<RobotLocation>
{

	volatile int currentX = 0;
	volatile int currentY = 0;

	@Test
	public void test() throws InterruptedException
	{
		RobotLocation locationMessage = new RobotLocation();
		locationMessage.addMessageListener(this);

		Thread.sleep(500000);

		// Thread th = new Thread(graph);
		// th.start();
	}

	@Override
	public void run()
	{

	}

	Integer setHeading = null;

	double currentSpeed = 0;

	MovingLidarObservationMultiBuffer buffer = new MovingLidarObservationMultiBuffer(1);

	WallFollowerUI ui = new WallFollowerUI();

	@Override
	public void onMessage(Message<RobotLocation> message)
	{
		RobotLocation messageObject = message.getMessageObject();
		int heading = (int) messageObject.getDeadReaconingHeading().getDegrees();
		if (setHeading == null)
		{
			setHeading = heading;
		}
		int speed = 100;

		System.out.println("Size "+messageObject.getObservations().size());

		buffer.addObservation(messageObject);

		Vector3D position = new Vector3D(messageObject.getX().convert(DistanceUnit.CM), messageObject.getY().convert(
				DistanceUnit.CM), 0);
		System.out.println(position);
		Collection<LidarObservation> laserData = buffer.getObservations(messageObject);

		ui.showPoints(laserData);

		double minX = 1000000;
		for (LidarObservation observation : laserData)
		{
			XY xy = new XY(observation.getX(), observation.getY());

			int x = (int) xy.getX();
			int y = (int) xy.getY();

			if (y < 40 && y > 0)
			{
				if (x > -15 && x < 15)
				{
					if (y < 50)
					{
						speed = 0;
					}
					System.out.println("Something directly ahead, turning right");
					setHeading = heading + 10;
					break;
				}
			}
		}
		if (setHeading.intValue() == heading)
		{
			int changeInHeading = 0;
			int changeCount = 0;
			for (LidarObservation observation : laserData)
			{
				XY xy = new XY(observation.getX(), observation.getY());

				int x = (int) xy.getX();
				int y = (int) xy.getY();
				if (y > 0)
				{

					if (y < 60 && x > -20 && x < 20)
					{
						minX = Math.min(minX, x);
					}

					if (y < 70)
					{

						if (x < -30 && x > -40)
						{
							changeInHeading += 10;
							changeCount++;
							System.out.println("Left of the groove,Turning right");

						}
						if (x < -50)
						{
							changeInHeading -= 10;
							changeCount++;
							System.out.println("Right of the groove, Turning left");

						}
					}

					// if
					// (message.getMessageObject().getClearSpaceAhead().convert(DistanceUnit.CM)
					// < 35)
					// {
					// speed = -50;
					// setHeading = heading + 45;
					// }
				}
			}
			setHeading = heading + (changeInHeading / Math.max(1, changeCount));
		}
		if ((setHeading.intValue() % 360 == heading || setHeading.intValue() == heading || setHeading.intValue() + 360 == heading)
				&& minX > -30)
		{
			// no negative x values were detected in the near range (y < 80)
			// and
			// no heading change has been
			// set, so set one... we'll turn left to try to find something
			setHeading = heading - 45;
			System.out.println("Nothing detected, turning left");
		}
		if (Math.abs(setHeading - heading) > 10)
		{
			speed = Math.min(speed, 0);
		}

		currentSpeed = speed / 10.0;
		SetMotion message2 = new SetMotion();
		message2.setSpeed(new Speed(new Distance(currentSpeed, DistanceUnit.CM), Time.perSecond()));
		message2.setHeading((double) setHeading);
	//	message2.publish();

	}
}