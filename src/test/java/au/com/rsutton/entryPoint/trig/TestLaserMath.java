package au.com.rsutton.entryPoint.trig;

import org.junit.Test;

public class TestLaserMath
{
	@Test
	public void test()
	{
		int vectorAngle = 35;
		int MAX_X_ANGLE = 45;

		double xadjust = vectorAngle * (0.5d / MAX_X_ANGLE);
		xadjust = 2d - xadjust;
		
		double angle = 84;

		double yDistance = xadjust / Math.tan(Math.toRadians(86d - angle));
		System.out.println(yDistance);

	}

}
