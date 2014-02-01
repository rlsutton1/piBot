package au.com.rsutton.hazelcast;

import org.junit.Test;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

public class HazelCastTest implements MessageListener<RobotLocation>
{
	private double x;
	private double y;
	private int heading;
	DistanceUnit unit = DistanceUnit.CM;

	@Test
	public void test1() throws InstantiationException, IllegalAccessException,
			InterruptedException
	{
		
		ResetCoords reset = new ResetCoords();
		reset.publish();

		TargetLocation targeter = new TargetLocation();
		
		
		//222/71.5 // quadrature/cm
		targeter.gotoTarget(new Distance(0,unit),new Distance( 30,unit));
		targeter.gotoTarget(new Distance(30,unit),new Distance( 30,unit));
		targeter.gotoTarget(new Distance(30,unit), new Distance(0,unit));
		targeter.gotoTarget(new Distance(0,unit), new Distance(0,unit));
		
		SetMotion message = new SetMotion();
		message.setHeading(0d);
		message.setSpeed((new Speed(new Distance(0, DistanceUnit.CM), Time
				.perSecond())));
		message.publish();

		Thread.sleep(40000);
	}

	@Override
	public void onMessage(Message<RobotLocation> message)
	{
		RobotLocation m = message.getMessageObject();

		x = m.getX().convert(unit);
		y = m.getY().convert(unit);
		heading = m.getHeading();

		System.out.println("Got message: " + message.getMessageObject());
	}
}