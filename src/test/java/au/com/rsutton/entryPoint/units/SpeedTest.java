package au.com.rsutton.entryPoint.units;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;
import au.com.rsutton.units.Speed;
import au.com.rsutton.units.Time;

public class SpeedTest
{

	@Test
	public void test()
	{
		Speed s1 = new Speed(new Distance(10, DistanceUnit.CM), new Time(1,
				TimeUnit.SECONDS));
		assertTrue(s1.getSpeed(DistanceUnit.MM, TimeUnit.SECONDS) == 100);

		Speed s2 = new Speed(new Distance(10, DistanceUnit.MM), new Time(100,
				TimeUnit.MILLISECONDS));
		assertTrue(
				"expected 100, got "
						+ s2.getSpeed(DistanceUnit.MM, TimeUnit.SECONDS),
				s2.getSpeed(DistanceUnit.MM, TimeUnit.SECONDS) == 100);

		
		Speed s3 = new Speed(new Distance(10, DistanceUnit.CM), new Time(1,
				TimeUnit.SECONDS));
		assertTrue(
				"expected 10, got "
						+ s3.getSpeed(DistanceUnit.CM, TimeUnit.SECONDS),
				s3.getSpeed(DistanceUnit.CM, TimeUnit.SECONDS) == 10);

	}
	
	@Test
	public void test2()
	{
		 DistanceUnit dUnit = DistanceUnit.MM;
		 TimeUnit tUnit = TimeUnit.SECONDS;

		Speed speed = new Speed(
				new Distance(3, DistanceUnit.MM),
				new Time(100, TimeUnit.MILLISECONDS));
		
		double r = speed.getSpeed(dUnit, tUnit);
		assertTrue(r==30);
		
		// average against our last speed
		double lastSpeed = speed.getSpeed(DistanceUnit.MM,TimeUnit.SECONDS);
		
		speed = new Speed(
				new Distance(lastSpeed, DistanceUnit.MM),
				new Time(1, TimeUnit.SECONDS));
		
		assertTrue(speed.getSpeed(dUnit, tUnit)==30);
		

	}

}
