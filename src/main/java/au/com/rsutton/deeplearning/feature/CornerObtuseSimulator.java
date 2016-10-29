package au.com.rsutton.deeplearning.feature;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;

public class CornerObtuseSimulator extends FeatureSimulatorBase
{
	public static final int OBTUSE_CORNER = 1;

	@Test
	public void test1()
	{

		for (int i = 0; i < 10; i++)
		{
			for (Vector3D vector : getLineScan().points)
			{
				System.out.println(vector);
				System.out.println("\n");
			}
		}
	}

	public CornerObtuseSimulator()
	{
		super(OBTUSE_CORNER, 20000);
		drawLine(0, 0, 1000, 0);
		drawLine(0, 0, 0, 1000);
	}

}
