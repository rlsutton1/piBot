package au.com.rsutton.navigation.graphslam;

import org.junit.Test;

public class GraphSlamMultiDimensionalTest
{

	@Test
	public void test()
	{
		DimensionXYTheta origin = new DimensionXYTheta(0, 0, 0);

		GraphSlamMultiDimensional<DimensionXYTheta> slam = new GraphSlamMultiDimensional<>(origin);

		DimensionXYTheta landmark1 = new DimensionXYTheta(5, 10, Math.toRadians(45));

		int landMark = slam.add(landmark1, new DimensionCertainty(new double[] {
				1, 1, 1, 1 }));

		slam.addMove(new DimensionXYTheta(1, 0, 0), new DimensionCertainty(new double[] {
				1, 1, 1, 1 }));

		landmark1 = new DimensionXYTheta(5, 10, Math.toRadians(35));

		slam.update(landMark, landmark1, new DimensionCertainty(new double[] {
				1, 1, 1, 1 }));

		for (DimensionXYTheta pos : slam.getPositions())
		{
			System.out.println(pos);
		}

	}

}
