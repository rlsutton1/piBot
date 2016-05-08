package au.com.rsutton.hazelcast;

import static org.junit.Assert.*;

import org.junit.Test;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;

public class HeadingHelperTest
{

	@Test
	public void test()
	{
		HeadingHelper tl = new HeadingHelper();
		double r = HeadingHelper.getChangeInHeading(270, 250);
		assertTrue("expect 20, got " + r, r == 20);
		
		r = HeadingHelper.getChangeInHeading(250, 270);
		assertTrue("expect -20, got " + r, r == -20);
		
		r = HeadingHelper.getChangeInHeading(10, 350);
		assertTrue("expect 20, got " + r, r == 20);
		
		r = HeadingHelper.getChangeInHeading(30, -270);
		assertTrue("expect -60, got " + r, r == -60);

	}
}
