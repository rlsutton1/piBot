package au.com.rsutton.robot.roomba;

import static org.junit.Assert.fail;

import org.junit.Test;

public class ColumnEvaluatorTest
{

	@Test
	public void test()
	{
		int x = 10;
		double yres = 200;
		float vfov = 1.021f;
		float hfov = 1.56f;
		double xres = 300;
		ColumnEvaluator evaluator = new ColumnEvaluator(hfov, vfov, xres, yres, x);
		int imageYCoord = 180;
		int distanceMM = 1000;
		evaluator.addPoint(imageYCoord, distanceMM);

		evaluator.addPoint(imageYCoord, 2000);

		evaluator.addPoint(20, 2000);

		fail("Not yet implemented");
	}

}
