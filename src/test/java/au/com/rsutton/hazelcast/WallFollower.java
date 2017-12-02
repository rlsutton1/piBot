package au.com.rsutton.hazelcast;

import java.util.Collection;

import org.junit.Test;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import au.com.rsutton.mapping.XY;
import au.com.rsutton.navigation.feature.RobotLocationDeltaListener;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.robot.lidar.LidarObservation;
import au.com.rsutton.robot.lidar.MovingLidarObservationMultiBuffer;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;
import au.com.rsutton.units.Speed;
import au.com.rsutton.units.Time;

public class WallFollower implements Runnable, MessageListener<RobotLocation>, RobotInterface
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

	Integer setHeading = 0;

	double currentSpeed = 0;

	MovingLidarObservationMultiBuffer buffer = new MovingLidarObservationMultiBuffer(1, this, null);

	WallFollowerUI ui = new WallFollowerUI();

	@Override
	public void onMessage(Message<RobotLocation> message)
	{
		RobotLocation messageObject = message.getMessageObject();

		int speed = 100;

		System.out.println("Size " + messageObject.getObservations().size());

		buffer.addObservation(messageObject.getObservations());

		Collection<LidarObservation> laserData = buffer.getObservations();

		ui.showPoints(laserData);

		double minX = 1000000;
		for (LidarObservation observation : laserData)
		{
			XY xy = new XY(observation.getX(), observation.getY());

			int x = xy.getX();
			int y = xy.getY();

			if (y < 40 && y > 0)
			{
				if (x > -15 && x < 15)
				{
					if (y < 50)
					{
						speed = 0;
					}
					System.out.println("Something directly ahead, turning right");
					setHeading = +10;
					break;
				}
			}
		}
		if (setHeading.intValue() == 0)
		{
			int changeInHeading = 0;
			int changeCount = 0;
			for (LidarObservation observation : laserData)
			{
				XY xy = new XY(observation.getX(), observation.getY());

				int x = xy.getX();
				int y = xy.getY();
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
			setHeading = (changeInHeading / Math.max(1, changeCount));
		}
		if (setHeading.intValue() == 0 && minX > -30)
		{
			// no negative x values were detected in the near range (y < 80)
			// and
			// no heading change has been
			// set, so set one... we'll turn left to try to find something
			setHeading = -45;
			System.out.println("Nothing detected, turning left");
		}
		if (Math.abs(setHeading) > 10)
		{
			speed = Math.min(speed, 0);
		}

		currentSpeed = speed / 10.0;
		SetMotion message2 = new SetMotion();
		message2.setSpeed(new Speed(new Distance(currentSpeed, DistanceUnit.CM), Time.perSecond()));
		message2.setChangeHeading(setHeading.doubleValue());
		// message2.publish();

	}

	@Override
	public void freeze(boolean b)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setSpeed(Speed speed)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void turn(double normalizeHeading)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void publishUpdate()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void addMessageListener(RobotLocationDeltaListener listener)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void removeMessageListener(RobotLocationDeltaListener listener)
	{
		// TODO Auto-generated method stub

	}
}