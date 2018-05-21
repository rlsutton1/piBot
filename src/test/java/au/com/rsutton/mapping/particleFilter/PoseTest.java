package au.com.rsutton.mapping.particleFilter;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Assert;
import org.junit.Test;

public class PoseTest
{

	@Test
	public void test()
	{
		for (int x = 0; x < 100; x += 10)
			for (int y = 0; y < 100; y += 10)
				for (int h = 0; h < 360; h += 10)
				{
					Pose pose = new Pose(x, y, h);

					Vector3D test = new Vector3D(x, y);
					Vector3D intermediate = pose.applyTo(test);
					Vector3D result = pose.applyInverseTo(intermediate);

					System.out.println("test " + test);
					System.out.println("intermeditate " + intermediate);
					System.out.println("result " + result);

					Assert.assertTrue(Math.abs(test.getX() - result.getX()) < 1);
					Assert.assertTrue(Math.abs(test.getY() - result.getY()) < 1);
					Assert.assertTrue(Math.abs(test.getY() - result.getY()) < 1);

				}
	}

}
