package au.com.rsutton.navigation.feature;

import static org.junit.Assert.fail;

import org.junit.Test;

public class GraphSlamFeatureTrackerTestZ
{

	@Test
	public void testZ()
	{
		GraphSlamFeatureTracker tracker = new GraphSlamFeatureTracker();
		int featurex = 40;
		int featurey = 40;
		int featureAngle = 90;

		tracker.setNewLocation(0, 0, 0, 1);

		int x = 0;
		int y = 0;
		for (int angle = 0; angle < 31; angle += 10)
		{

			tracker.setNewLocation(0, 00, 0, 1);

		}
		fail("Not yet implemented");
	}

}
