package au.com.rsutton.mapping.v3.impl;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.mapping.XY;
import au.com.rsutton.robot.rover.Angle;
import au.com.rsutton.robot.rover.AngleUnits;

/**
 * a feature is an abrupt change in the distance when scanning like a radar
 * does, this will relate to corners of objects in the environment
 * 
 * @author rsutton
 *
 */
public class Feature
{

	XY location;
	
	public Feature(XY xy)
	{
		location = xy;
	}

	FeatureRelationship createRelationShip(Feature feature)
	{

		// create relationship
		Vector3D locationVector = new Vector3D(location.getX(),
				location.getY(), 0);
		Vector3D otherLocationVector = new Vector3D(feature
				.getFeatureLocation().getX(), feature.getFeatureLocation()
				.getY(), 0);
		double distance = Vector3D
				.distance(locationVector, otherLocationVector);
		Angle angle = new Angle(Vector3D.angle(locationVector,
				otherLocationVector), AngleUnits.RADIANS);
		return new FeatureRelationship(this, feature, angle, distance);

	}

	XY getFeatureLocation()
	{
		return location;
	}

}
