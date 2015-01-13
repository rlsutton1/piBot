package au.com.rsutton.robot.rover;

import static org.junit.Assert.*;

import org.junit.Test;

public class AngleTest
{

	@Test
	public void test()
	{
		for (double a = 0; a < 360; a++)
		{
			Angle angle = new Angle(a, AngleUnits.DEGREES);
			assertTrue("exptected " + a + ", got " + angle.getDegrees(),
					Math.abs(angle.getDegrees()-a)< 0.5);
		}
	}

}
