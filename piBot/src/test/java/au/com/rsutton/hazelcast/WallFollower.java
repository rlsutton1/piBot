package au.com.rsutton.hazelcast;

import java.util.Collection;

import org.junit.Test;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;
import au.com.rsutton.mapping.LaserRangeConverter;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.pi4j.gpio.extension.pixy.PixyCoordinate;

public class WallFollower implements Runnable, MessageListener<RobotLocation>
{

	volatile int currentX = 0;
	volatile int currentY = 0;
	LaserRangeConverter converter = new LaserRangeConverter();

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

	@Override
	public void onMessage(Message<RobotLocation> message)
	{
		RobotLocation messageObject = message.getMessageObject();
		int heading = messageObject.getHeading();
		if (setHeading == null)
		{
			setHeading = heading;
		}
		int speed = 100;

		Collection<PixyCoordinate> laserData = messageObject.getLaserData();
		double minX = 1000000;
		for (PixyCoordinate vector : laserData)
		{
			double observationAngle = converter.convertAngle(vector
					.getAverageX());
			// if (vector.angle > -10 && vector.angle < 30)
			{
				Integer convertedRange = converter.convertRange(
						(int) vector.getAverageX(), (int) vector.getAverageY());
				if (convertedRange != null)
				{
					Distance distance = new Distance(convertedRange,
							DistanceUnit.CM);

					double x = Math.sin(Math.toRadians(observationAngle))
							* distance.convert(DistanceUnit.CM);
					double y = Math.cos(Math.toRadians(observationAngle))
							* distance.convert(DistanceUnit.CM);

					if (y < 100 && x > -40 && x < 40)
					{
						if (y < 50)
						{
							speed = -50;
						}
						setHeading = heading + 45;
						break;
					}
					if (x < -30 && x > -50)
					{
						setHeading = heading + 10;
						break;
					}
					if (x < -50)
					{
						setHeading = heading - 10;
						break;
					}
					if (y < 60)
					{
						minX = Math.min(minX, x);
					}

				}
			}
		}

		if (message.getMessageObject().getClearSpaceAhead()
				.convert(DistanceUnit.CM) < 35)
		{
			speed = -50;
			setHeading = heading + 45;
		}

		if (setHeading.intValue() == heading && minX > -30)
		{
			// no negative x values were detected in the near range (y < 80) and no heading change has been
			// set, so set one... we'll turn left to try to find something
			 setHeading = heading - 45;
		}

		if (Math.abs(setHeading - heading) > 10)
		{
			speed = Math.min(speed, 0);
		}
		SetMotion message2 = new SetMotion();
		message2.setSpeed(new Speed(new Distance(speed, DistanceUnit.CM), Time
				.perSecond()));
		message2.setHeading((double) setHeading);
		message2.publish();

	}
}