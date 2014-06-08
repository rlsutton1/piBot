package au.com.rsutton.hazelcast;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Test;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.pi4j.gpio.extension.pixy.PixyCoordinate;

public class CalabratePixy implements MessageListener<RobotLocation>
{
	volatile private int heading = 10000;
	volatile private Distance distance;

	LinkedBlockingQueue<Collection<PixyCoordinate>> queuedLaserData = new LinkedBlockingQueue<>();

	@Test
	public void gotoTarget() throws InstantiationException,
			IllegalAccessException, InterruptedException
	{

		// drive robot to gather calabration data....

		SetMotion message = new SetMotion();

		RobotLocation locationMessage = new RobotLocation();
		locationMessage.addMessageListener(this);
		System.out.println("Waiting for initial heading");
		while (heading == 10000)
		{
			Thread.sleep(100);
		}
		int setHeading = heading;
		message.setHeading((double) setHeading);
		for (int setDistance = 35; setDistance < 200; setDistance += 3)
		{
			while (!checkDistance(setDistance))
			{
				System.out.println("moving to " + setDistance);
				while (!checkDistance(setDistance))
				{
					int speed = ((int) distance.convert(DistanceUnit.CM))
							- setDistance;

					if (speed > 0)
					{
						speed = Math.min(5, speed);
					} else
					{
						speed = Math.max(-5, speed);
					}
					message.setSpeed(new Speed(new Distance(speed,
							DistanceUnit.CM), Time.perSecond()));
					message.publish();

					Thread.sleep(100);
				}
				message.setSpeed(new Speed(new Distance(0, DistanceUnit.CM),
						Time.perSecond()));
				message.publish();

				System.out.println("waiting for heading to settle");
				for (int i = 0; i < 4; i++)
				{
					while (heading < setHeading + 1 && heading > setHeading - 1)
					{
						Thread.sleep(100);
					}
					Thread.sleep(500);
				}
			}
			queuedLaserData.clear();
			System.out.println("Collecting data");

			int ctr = 0;
			int points = 4;
			int samples = 15;
			while (queuedLaserData.size() < points * samples
					&& ctr < (points * samples * 2))
			{
				Thread.sleep(250);
				ctr++;
			}
			System.out.println("Analysing data (NOT) got "
					+ queuedLaserData.size() + " points");
			// TODO: analyse laser data

		}

	}

	private boolean checkDistance(int setDistance)
	{
		int dist = (int) distance.convert(DistanceUnit.CM);
		int percentageOfSetDistance = (setDistance * 4) / 100;
		percentageOfSetDistance = Math.min(2, percentageOfSetDistance);
		return dist < setDistance + percentageOfSetDistance
				&& dist > setDistance - percentageOfSetDistance;
	}

	@Override
	public void onMessage(Message<RobotLocation> message)
	{
		RobotLocation m = message.getMessageObject();

		heading = m.getHeading();
		distance = m.getClearSpaceAhead();
		queuedLaserData.add(m.getLaserData());

	}
}