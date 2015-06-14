package au.com.rsutton.mapping;

import static org.junit.Assert.*;

import org.junit.Test;

import au.com.rsutton.cv.RangeFinderConfiguration;

public class CoordResolverTest
{

	@Test
	public void test()
	{
		RangeFinderConfiguration rangeFinderConfig = new RangeFinderConfiguration.Builder()
				.setCameraResolution(544, 288).setYMaxDegrees(24)
				.setYZeroDegrees(242).setXFieldOfViewRangeDegrees(60)
				.setCameraLaserSeparation(110).setOrientationToRobot(-28)
				.build();

		CoordResolver resolver = new CoordResolver(rangeFinderConfig);

		for (double y = 25; y < 242; y++)
		{
			double angle = resolver.laserAboveCamra(y);
			double newY = resolver.convertAngleToCamraYCoord(angle);

			System.out.println(y + " " + newY);
			assertTrue("expectged " + y + " got " + newY,
					Math.abs(y - newY) < 1.0);
		}
	}

	@Test
	public void testLineHeight()
	{
		RangeFinderConfiguration rangeFinderConfig = new RangeFinderConfiguration.Builder()
				.setCameraResolution(544, 288).setYMaxDegrees(24)
				.setYZeroDegrees(242).setXFieldOfViewRangeDegrees(60)
				.setCameraLaserSeparation(110).setOrientationToRobot(-28)
				.build();

		CoordResolver resolver = new CoordResolver(rangeFinderConfig);

		for (double y = 20; y < 240; y++)
		{
			System.out.println("line height: "+y+" "+resolver.getExpectedLineHeight(y));

		}
	}
}
