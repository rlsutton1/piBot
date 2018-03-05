package au.com.rsutton.angle;

import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class AngleUtilTest
{

	@Test
	public void testNormalize0to360()
	{
		for (double i = 0; i < 360; i++)
		{
			assertTrue(i == AngleUtil.normalize(i));
		}

	}

	@Test
	public void testNormalize360to720()
	{
		for (double i = 360; i < 720; i++)
		{
			assertTrue(i - 360 == AngleUtil.normalize(i));
		}

	}

	@Test
	public void testNormalizeNegative359to0()
	{
		for (double i = -359; i < 0; i++)
		{
			assertTrue(i + 360 == AngleUtil.normalize(i));
		}

	}

	@Test
	public void testDelta180with0to360()
	{
		for (double i = 0; i < 360; i++)
		{
			double ret = AngleUtil.delta(180, i);
			assertTrue("" + ret, ret >= -180 && ret < 180);
			assertTrue("Expected " + (i - 180) + " but got " + ret + " for " + i, i - 180 == ret);
			assertTrue("" + ret, 180 + ret == i);
		}
	}

	@Test
	public void testAverageAngle()
	{
		List<Double> angles = new LinkedList<>();
		angles.add(45d);
		angles.add(90d);
		double ret = AngleUtil.getAverageAngle(angles);

		assertTrue("" + ret, ret == (45.0 + 90.0) / 2.0);

	}

	@Test
	public void testAverageAngles45and315equals0()
	{
		List<Double> angles = new LinkedList<>();
		angles.add(45d);
		angles.add(315d);
		double ret = AngleUtil.getAverageAngle(angles);

		assertTrue("" + ret, ret > -1 && ret < 1);

	}

	@Test
	public void testAverageAngles90and270equals180()
	{
		List<Double> angles = new LinkedList<>();
		angles.add(90d);
		angles.add(270d);
		double ret = AngleUtil.getAverageAngle(angles);

		assertTrue("" + ret, ret == 180);

	}

	@Test
	public void testAverageAnglesRandomNear180()
	{
		for (int j = 0; j < 100; j++)
		{
			List<Double> angles = new LinkedList<>();
			for (int i = 0; i < 100; i++)
			{
				angles.add(180 + Math.random() * 30);
			}

			double ret = AngleUtil.getAverageAngle(angles);

			assertTrue("" + ret, ret > 170 && ret < 210);
		}
	}

	@Test
	public void testAverageAnglesRandomNear0()
	{
		for (int j = 0; j < 100; j++)
		{
			List<Double> angles = new LinkedList<>();
			for (int i = 0; i < 100; i++)
			{
				angles.add(-30 + Math.random() * 60);
			}

			double ret = AngleUtil.getAverageAngle(angles);

			assertTrue("" + ret, ret > 330 || ret < 30);
		}
	}

	@Test
	public void testAverageAnglesEquals45()
	{
		List<Double> angles = new LinkedList<>();
		angles.add(-90d);
		angles.add(90d);
		angles.add(45d);
		double ret = AngleUtil.getAverageAngle(angles);

		assertTrue("" + ret, ret > 44.5 && ret < 45.5);

	}

	@Test
	public void testAverageAnglesEquals1()
	{
		List<Double> angles = new LinkedList<>();
		angles.add(90d);
		angles.add(0d);
		angles.add(0d);
		double ret = AngleUtil.getAverageAngle(angles);

		assertTrue("" + ret, ret > 26 && ret < 27);

	}

	@Test
	public void testAverageAnglesEquals2()
	{
		List<Double> angles = new LinkedList<>();
		angles.add(90d);
		angles.add(0d);
		angles.add(0d);
		angles.add(0d);
		double ret = AngleUtil.getAverageAngle(angles);

		assertTrue("" + ret, ret > 18 && ret < 19);

	}

}
