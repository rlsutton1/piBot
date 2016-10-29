package au.com.rsutton.deeplearning.feature;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;

public class CornerAcuteSimulator extends FeatureSimulatorBase
{
	public static final int ACUTE_CORNER = 2;

	@Test
	public void test1()
	{

		for (int i = 0; i < 10; i++)
		{
			for (Vector3D vector : getLineScan().points)
			{
				System.out.println(vector);

			}
			System.out.println("\n");
		}
	}

	public CornerAcuteSimulator()
	{
		super(ACUTE_CORNER, 20000);
		drawLine(0, 0, -1000, 0);
		drawLine(0, 0, 0, -1000);

		drawLine(1000, 1000, 1010, 1010);
	}

}
