package au.com.rsutton.angle;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import au.com.rsutton.units.Angle;
import au.com.rsutton.units.AngleUnits;
import au.com.rsutton.units.HeadingHelper;

public class HeadingChangeError
{

	@Test
	public void test1()
	{

		for (int i = -720; i < 720; i += 20)

		{

			int last = i - 10;
			Angle lastAngle = new Angle(HeadingHelper.normalizeHeading(last), AngleUnits.DEGREES);
			Angle currentAngle = new Angle(HeadingHelper.normalizeHeading(i), AngleUnits.DEGREES);

			assertTrue(lastAngle.difference(currentAngle) == 10);
			assertTrue(currentAngle.difference(lastAngle) == -10);

		}
		//
		// return -lastheading.difference(new
		// Angle(HeadingHelper.normalizeHeading(robotLocation.getDeadReaconingHeading()
		// .getDegrees()), AngleUnits.DEGREES));

	}
}
