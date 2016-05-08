package au.com.rsutton.mapping.v3.impl;

import au.com.rsutton.robot.rover.Angle;

/**
 * describes the relationship between a feature and the surrounding features in
 * terms of distances and angles
 * 
 * @author rsutton
 *
 */
public class FeatureRelationship
{

	Feature primaryFeature;

	Feature secondaryFeature;

	Angle angleFromPrimaryToSeconday;

	double distanceBetweenFeatures;

	FeatureRelationship(Feature primary, Feature secodary,
			Angle angleFromPrimaryToSeconday, double distanceBetweenFeatures)
	{
		primaryFeature = primary;
		secondaryFeature = secodary;
		this.angleFromPrimaryToSeconday = angleFromPrimaryToSeconday;
		this.distanceBetweenFeatures = distanceBetweenFeatures;
	}

	public Feature getPrimaryFeature()
	{
		return primaryFeature;
	}

	public Feature getSecondaryFeature()
	{
		return secondaryFeature;
	}

	public Angle getAngleFromPrimaryToSeconday()
	{
		return angleFromPrimaryToSeconday;
	}

	public double getDistanceBetweenFeatures()
	{
		return distanceBetweenFeatures;
	}
}
