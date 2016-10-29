package au.com.rsutton.deeplearning.feature;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;

public class LineSimulator extends FeatureSimulatorBase
{

	public static final int LINE = 3;

	@Test
	public void test1()
	{

		for (int i = 0; i < 10; i++)
		{
			for (Vector3D vector : getLineScan().points)
			{
				System.out.println(vector);
			}
			System.out.println();
		}
	}

	public LineSimulator()
	{
		super(LINE, 20000);
		drawLine(-1000, -1000, 1000, 1000);
	}

}
