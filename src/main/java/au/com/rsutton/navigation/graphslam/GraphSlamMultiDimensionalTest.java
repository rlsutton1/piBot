package au.com.rsutton.navigation.graphslam;

import org.junit.Test;

import au.com.rsutton.navigation.graphslam.DimensionXYTheta.ComponentAngle;

public class GraphSlamMultiDimensionalTest
{

	@Test
	public void test()
	{
		DimensionXYTheta origin = new DimensionXYTheta(1, 0, ComponentAngle.createComponentAngle(0));

		GraphSlamMultiDimensional<DimensionXYTheta> slam = new GraphSlamMultiDimensional<>(origin);

		// slam.setNewLocation(new DimensionXYTheta(1, 0, 0), new
		// DimensionCertainty(new double[] {
		// 1, 1, 1, 1 }));

		int landMarkp1 = slam.add(new DimensionXYTheta(0, 10, ComponentAngle.createComponentAngleDelta(0, 0)),
				new DimensionCertainty(new double[] {
						1, 1, 1, 1 }));

		for (DimensionXYTheta pos : slam.getPositions())
		{
			System.out.println(" output " + pos);
		}

		slam.update(landMarkp1, new DimensionXYTheta(10, 0, ComponentAngle.createComponentAngleDelta(90, 90)),
				new DimensionCertainty(new double[] {
						1, 1, 1, 1 }));

		// DimensionXYTheta landmark1 = new DimensionXYTheta(5, 10,
		// ComponentAngle.createComponentAngleDelta(0, 45));

		// slam.update(landMark, landmark1, new DimensionCertainty(new double[]
		// {
		// 1, 1, 1, 1 }));

		for (DimensionXYTheta pos : slam.getPositions())
		{
			System.out.println(" output " + pos);
		}

	}

}
