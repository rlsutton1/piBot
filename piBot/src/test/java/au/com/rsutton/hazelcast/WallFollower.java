package au.com.rsutton.hazelcast;

import java.util.Collection;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;
import au.com.rsutton.mapping.XY;
import au.com.rsutton.robot.rover.LidarObservation;
import au.com.rsutton.robot.rover.MovingLidarObservationMultiBuffer;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

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

	MovingLidarObservationMultiBuffer buffer = new MovingLidarObservationMultiBuffer();

	WallFollowerUI ui = new WallFollowerUI();
	
	@Override
	public void onMessage(Message<RobotLocation> message)
	{
		RobotLocation messageObject = message.getMessageObject();
		int heading = (int) messageObject.getHeading().getDegrees();
		if (setHeading == null)
		{
			setHeading = heading;
		}
		int speed = 100;

		buffer.addObservation(messageObject);

		Collection<LidarObservation> laserData = buffer.getObservations(
				new Vector3D(messageObject.getX().convert(DistanceUnit.CM), messageObject.getY().convert(
						DistanceUnit.CM)), new Rotation(RotationOrder.XYZ, 0, 0, messageObject.getHeading()
						.getRadians()));

		ui.showPoints(laserData);
		
		double minX = 1000000;

		
		
		for (LidarObservation observation : laserData)
		{
			XY xy = new XY(observation.getX(), observation.getY());

			int x = (int) xy.getX();
			int y = (int) xy.getY();
			System.out.println(System.currentTimeMillis()+" "+x + " " + y);
			if (y < 70 && x > -30 && x < 30)
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

//			if (message.getMessageObject().getClearSpaceAhead().convert(DistanceUnit.CM) < 35)
//			{
//				speed = -50;
//				setHeading = heading + 45;
//			}

			if (setHeading.intValue() == heading && minX > -30)
			{
				// no negative x values were detected in the near range (y < 80)
				// and
				// no heading change has been
				// set, so set one... we'll turn left to try to find something
				setHeading = heading - 45;
			}
		}
		if (Math.abs(setHeading - heading) > 10)
		{
			speed = Math.min(speed, 0);
		}

		currentSpeed = speed;
		SetMotion message2 = new SetMotion();
		message2.setSpeed(new Speed(new Distance(currentSpeed, DistanceUnit.CM), Time.perSecond()));
		message2.setHeading((double) setHeading);
		message2.publish();

	}
}