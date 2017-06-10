package au.com.rsutton.navigation.feature;

import static org.junit.Assert.fail;

import org.junit.Test;

public class GraphSlamFeatureTrackerTest
{

	@Test
	public void testX()
	{
		GraphSlamFeatureTracker tracker = new GraphSlamFeatureTracker();
		int featurex = 40;
		int featurey = 40;
		int featureAngle = 45;

		tracker.setNewLocation(0, 0, 0, 1);

		int y = 0;
		int angle = 0;
		for (int x = 0; x < 40; x += 10)
		{

			double xToF = featurex - x;
			double yToF = featurey - y;
			double absoluteAngleToFeature = Math.toDegrees(Math.atan2(yToF, xToF));
			double relativeAngle = absoluteAngleToFeature - angle;

			tracker.addObservation(new Feature(xToF, yToF, relativeAngle, 1, FeatureType.CONVEX), angle, 1);

			tracker.setNewLocation(10, 0, 0, 0.5);

		}
		fail("Not yet implemented");
	}

	@Test
	public void testY()
	{
		GraphSlamFeatureTracker tracker = new GraphSlamFeatureTracker();
		int featurex = 40;
		int featurey = 40;
		int featureAngle = 45;

		tracker.setNewLocation(0, 0, 0, 1);

		int x = 0;
		int angle = 0;
		for (int y = 0; y < 40; y += 10)
		{
			double xToF = featurex - x;
			double yToF = featurey - y;
			double absoluteAngleToFeature = Math.toDegrees(Math.atan2(yToF, xToF));
			double relativeAngle = absoluteAngleToFeature - angle;

			tracker.addObservation(new Feature(xToF, yToF, relativeAngle, 1, FeatureType.CONVEX), angle, 1);

			tracker.setNewLocation(0, 10, 0, 0.5);

		}
		fail("Not yet implemented");
	}

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
		for (int angle = 0; angle < 40; angle += 10)
		{
			System.out.println("Angle " + angle);
			double xToF = featurex - x;
			double yToF = featurey - y;
			double absoluteAngleToFeature = Math.toDegrees(Math.atan2(yToF, xToF));
			double relativeAngle = absoluteAngleToFeature - angle;
			System.out.println("Relative angle " + relativeAngle);

			tracker.addObservation(new Feature(xToF, yToF, featureAngle - relativeAngle, 1, FeatureType.CONVEX), angle,
					1);

			tracker.setNewLocation(0, 00, 0, 0.5);

		}
		fail("Not yet implemented");
	}

}
