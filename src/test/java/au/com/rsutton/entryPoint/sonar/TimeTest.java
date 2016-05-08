package au.com.rsutton.entryPoint.sonar;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import au.com.rsutton.entryPoint.units.Time;

public class TimeTest
{

	@Test
	public void test()
	{
		Time time = new Time(10, TimeUnit.SECONDS);
		Time time2 = new Time(20000, TimeUnit.MILLISECONDS);

		assertTrue(
				""
						+ (time2.convert(TimeUnit.SECONDS) - time
								.convert(TimeUnit.SECONDS)),
				time2.convert(TimeUnit.SECONDS)
						- time.convert(TimeUnit.SECONDS) == 10);

		
	}

}
