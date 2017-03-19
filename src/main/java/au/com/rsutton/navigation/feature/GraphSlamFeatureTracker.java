package au.com.rsutton.navigation.feature;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.navigation.graphslam.DimensionCertainty;
import au.com.rsutton.navigation.graphslam.DimensionXYTheta;
import au.com.rsutton.navigation.graphslam.GraphSlamMultiDimensional;

public class GraphSlamFeatureTracker
{

	Map<Integer, Spike> featureMap = new HashMap<>();

	DimensionXYTheta currentLocation = new DimensionXYTheta(0, 0, 0);

	GraphSlamMultiDimensional<DimensionXYTheta> slam = new GraphSlamMultiDimensional<>(currentLocation);

	public void setNewLocation(double dx, double dy, double dt, double certain)
	{

		DimensionCertainty certainty = new DimensionCertainty(new double[] {
				certain, certain, certain, certain });
		DimensionXYTheta dimension = new DimensionXYTheta(dx, dy, dt);
		slam.setNewLocation(dimension, certainty);

		refreshFeatureMapFromSlam();

	}

	public void addObservation(Spike newObservation)
	{

		Spike absoluteSpike = new Spike(currentLocation.getX() + newObservation.x,
				currentLocation.getY() + newObservation.y, newObservation.angle, newObservation.getAngleAwayFromWall());

		double x = absoluteSpike.x;
		double y = absoluteSpike.y;

		double maxMatchDistance = 50;
		Map<Integer, Spike> nearFeatures = findFeaturesNear(absoluteSpike, maxMatchDistance, 45);
		if (!nearFeatures.isEmpty())
		{
			for (Entry<Integer, Spike> matched : nearFeatures.entrySet())
			{
				double distance = Vector3D.distance(new Vector3D(x, y, 0),
						new Vector3D(matched.getValue().x, matched.getValue().y, 0));

				double error = Math.max(1.0 - ((distance) / maxMatchDistance), .01);

				// TODO: apportion certainty to quality of match
				update(matched.getKey(), newObservation, error);

			}
		} else
		{
			// dont allow adding distant features

			double distance = Math.sqrt(Math.pow(newObservation.x, 2) + Math.pow(newObservation.y, 2));

			// disallow adding distant features
			if (distance < 200)
			{
				addNewFeature(newObservation);
			}
		}
		refreshFeatureMapFromSlam();
	}

	int addNewFeature(Spike feature)
	{
		DimensionCertainty certainty = new DimensionCertainty(new double[] {
				1, 1, 1, 1 });
		DimensionXYTheta dimension = new DimensionXYTheta(feature.x, feature.y, feature.angle);
		int position = slam.add(dimension, certainty);
		featureMap.put(position, feature);
		return position;
	}

	void update(int position, Spike feature, double matchCertainty)
	{

		double maxDistance = 500;
		double distance = Math.sqrt(Math.pow(feature.x, 2) + Math.pow(feature.y, 2));
		double c = matchCertainty * ((maxDistance - distance) / maxDistance);

		DimensionCertainty certainty = new DimensionCertainty(new double[] {
				c, c, c, c });
		DimensionXYTheta dimension = new DimensionXYTheta(feature.x, feature.y, feature.angle);
		slam.update(position, dimension, certainty);

	}

	void refreshFeatureMapFromSlam()
	{
		List<DimensionXYTheta> newData = slam.getPositions();

		currentLocation = newData.get(1);
		System.out.println("Current location is " + currentLocation);

		int pos = 0;
		for (DimensionXYTheta position : newData)
		{
			// this is wrong
			Spike feature = featureMap.get(pos);
			try
			{
				if (feature != null)
				{
					// System.out.println("old feature " + pos + " " + feature);

					// feature.angle = position.getTheta();
					feature.x = position.getX();
					feature.y = position.getY();

					// System.out.println("new feature " + pos + " " + feature);
				}
			} catch (Exception e)
			{
				throw e;
			}
			pos++;
		}
		// slam.dumpPositions();
	}

	Map<Integer, Spike> findFeaturesNear(Spike feature, double range, double angleRange)
	{
		Map<Integer, Spike> result = new HashMap<>();

		for (Entry<Integer, Spike> entry : featureMap.entrySet())
		{
			Spike spike = entry.getValue();
			if (feature.getDistance(spike) < range)
			{
				// within distance
				if (HeadingHelper.getChangeInHeading(feature.angle, spike.angle) < angleRange)
				{
					// correct angle
					if (feature.angleAwayFromWall == spike.angleAwayFromWall)
					{
						// correct orientation
						Integer position = entry.getKey();
						result.put(position, spike);

					}
				}

			}
		}

		return result;
	}
}
