package au.com.rsutton.navigation.feature;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.mapping.particleFilter.ScanObservation;

public class FeatureExtractorCorner extends FeatureExtractor
{

	@Override
	List<Spike> detectSpike(List<ScanObservation> lastObs3)
	{
		List<Spike> ret = new LinkedList<>();

		List<ScanObservation> lastObs2 = resampleData(lastObs3, 5);
		if (lastObs2.size() == 5)
		{
			// check total distance between ends is less than 50cm
			if (Vector3D.distance(lastObs2.get(0).getVector(), lastObs2.get(4).getVector()) < 50)
			{
				List<Double> angles = convertFromPointsToAngles(lastObs2);

				double d1 = angles.get(0);
				double d2 = angles.get(1);
				double d3 = angles.get(2);
				double d4 = angles.get(3);

				// check for 2 reasonably straight lines and a strong change in
				// angle
				boolean hasPlane1 = Math.abs(HeadingHelper.getChangeInHeading(d2, d1)) < 30;
				boolean hasPlane2 = Math.abs(HeadingHelper.getChangeInHeading(d3, d4)) < 30;
				boolean hasAngle = Math.abs(HeadingHelper.getChangeInHeading(d2, d3)) > 60;

				if (hasPlane1 && hasPlane2 && hasAngle)
				{
					if (HeadingHelper.getChangeInHeading(d2, d3) < 0)
					{

						int deltaY = lastObs2.get(2).getY() - lastObs2.get(0).getY();
						int deltaX = lastObs2.get(2).getX() - lastObs2.get(0).getX();

						double angleAwayFromWall = Math.toDegrees(Math.atan2(deltaY, deltaX));

						// concave corner
						ret.add(new Spike(lastObs2.get(2).getX(), lastObs2.get(2).getY(), d1 + 90, angleAwayFromWall));
						ret.add(new Spike(lastObs2.get(2).getX(), lastObs2.get(2).getY(), d4 + 90,
								angleAwayFromWall - 90));
					} else
					{
						int deltaY = lastObs2.get(2).getY() - lastObs2.get(3).getY();
						int deltaX = lastObs2.get(2).getX() - lastObs2.get(3).getX();

						double angleAwayFromWall = Math.toDegrees(Math.atan2(deltaY, deltaX));

						// convex corner
						ret.add(new Spike(lastObs2.get(2).getX(), lastObs2.get(2).getY(), d1 - 90,
								angleAwayFromWall + 90));
						ret.add(new Spike(lastObs2.get(2).getX(), lastObs2.get(2).getY(), d4 - 90,
								angleAwayFromWall + 180));

					}
				}
			}
		}
		return ret;

	}

	List<Double> convertFromPointsToAngles(List<ScanObservation> list)
	{
		List<Double> results = new LinkedList<>();

		Double lastAngle = null;

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
