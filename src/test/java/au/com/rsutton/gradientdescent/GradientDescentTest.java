package au.com.rsutton.gradientdescent;

import static org.junit.Assert.fail;

import org.junit.Test;

public class GradientDescentTest
{

	@Test
	public void test()
	{
		GradientDescent gd = new GradientDescent(new GradientDescent.TestFunction(), new double[] {
				0, 0, 1 });

		gd.descend(0.1, 1);
		fail("Not yet implemented");
	}

}
