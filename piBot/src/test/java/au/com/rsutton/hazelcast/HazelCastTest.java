package au.com.rsutton.hazelcast;

import org.junit.Test;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;

public class HazelCastTest
{
	DistanceUnit unit = DistanceUnit.CM;

	@Test
	public void test1() throws InstantiationException, IllegalAccessException,
			InterruptedException
	{

		TargetLocation targeter = new TargetLocation();

		int size = 50;

		// 222/71.5 // quadrature/cm
		targeter.gotoTarget(new Distance(0, unit), new Distance(size, unit));
		targeter.gotoTarget(new Distance(size, unit), new Distance(size, unit));
		targeter.gotoTarget(new Distance(size, unit), new Distance(0, unit));
		targeter.gotoTarget(new Distance(0, unit), new Distance(0, unit));

		SetMotion message = new SetMotion();
		message.setHeading(0d);
		message.setSpeed((new Speed(new Distance(0, DistanceUnit.CM), Time
				.perSecond())));
		message.publish();

		Thread.sleep(40000);
	}

}