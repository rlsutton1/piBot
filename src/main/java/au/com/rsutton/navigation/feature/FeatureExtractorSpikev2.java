package au.com.rsutton.navigation.feature;

import java.util.LinkedList;
import java.util.List;

import au.com.rsutton.mapping.particleFilter.ScanObservation;
import au.com.rsutton.robot.RobotInterface;

public class FeatureExtractorSpikev2 extends FeatureExtractor
{

	public FeatureExtractorSpikev2(SpikeListener listener, RobotInterface robot)
	{
		super(listener, robot);
	}

	double sumAngles(List<Double> angles)
	{
		Double a1 = null;
		double variance = 0;
		for (Double angle : angles)
		{
			if (a1 == null)
			{
				a1 = angle;
			}
			variance += Math.abs(a1 - angle);
		}
		return variance;
	}

	@Override
	List<Feature> detectFeature(List<ScanObservation> lastObs3)
	{
		List<Feature> ret = new LinkedList<>();

		int samples = 8;
		int c1 = 3;
		int c2 = 4;
		int e2 = samples - 1;

		List<ScanObservation> lastObs2 = resampleData(lastObs3, samples);
		if (lastObs2.size() == samples)
		{
			// check total distance between ends is less than 50cm
			ScanObservation plane1end = lastObs2.get(0);
			ScanObservation plane1center = lastObs2.get(c1);
			ScanObservation plane2center = lastObs2.get(c2);
			ScanObservation plane2end = lastObs2.get(e2);

			List<Double> angles = convertFromPointsToAngles(lastObs2);

			double variance1 = sumAngles(angles.subList(0, c1));
			double variance2 = sumAngles(angles.subList(c2, e2));

			double dx = plane1center.getX() - plane2center.getX();
			double dy = plane1center.getY() - plane2center.getY();
			double rift = Math.sqrt((dx * dx) + (dy * dy));

			double d3 = plane1center.getDisctanceCm();
			double d4 = plane2center.getDisctanceCm();

			// check for 2 reasonably straight lines and a strong change in
			// angle

			if (variance1 < 30 && variance2 < 30 && rift > 50)
			{
				double deltaY;
				double deltaX;
				double angleAwayFromWall;
				double x;
				double y;
				if (d3 < d4)
				{
					// use plane 1

					deltaY = plane1center.getY() - plane1end.getY();
					deltaX = plane1center.getX() - plane1end.getX();
					x = plane1center.getX();
					y = plane1center.getY();
					angleAwayFromWall = Math.toDegrees(Math.atan2(deltaY, deltaX)) - 90;
				} else
				{
					// use plane 2

					deltaY = plane2center.getY() - plane2end.getY();
					deltaX = plane2center.getX() - plane2end.getX();
					x = plane2center.getX();
					y = plane2center.getY();
					angleAwayFromWall = Math.toDegrees(Math.atan2(deltaY, deltaX)) + 90;

				}
				double angle = Math.toDegrees(Math.atan2(deltaY, deltaX));

				ret.add(new Feature(x, y, angle, angleAwayFromWall, FeatureType.CONVEX));

			}
		}

		return ret;

	}

	List<Double> convertFromPointsToAngles(List<ScanObservation> list)
	{
		List<Double> results = new LinkedList<>();

		for (int i = 1; i < list.size(); i++)
		{
			double deltaX = list.get(i - 1).getX() - list.get(i).getX();
			double deltaY = list.get(i - 1).getY() - list.get(i).getY();
			// double length = Vector3D.distance(list.get(i - 1), list.get(i));
			double degrees = Math.toDegrees(Math.atan2(deltaY, deltaX));
			results.add(degrees);

		}
		return results;

	}
}
