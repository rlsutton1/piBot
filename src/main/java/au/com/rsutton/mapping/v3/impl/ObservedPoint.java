package au.com.rsutton.mapping.v3.impl;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.mapping.XY;

/**
 * an observation reading including xy coordinates and accuracy
 * 
 * @author rsutton
 *
 */
public class ObservedPoint
{

	ObservationOrigin origin;

	XY location;

	Distance distance;

	double accuracy;

	public ObservedPoint(XY location, ObservationOrigin origin, Distance distance)
	{
		this.location = location;
		this.origin = origin;
		this.distance = distance;
	}

	/**
	 * location the observation was taken from... says something about what is
	 * hidden by other geometries
	 * 
	 * @return
	 */
	public ObservationOrigin getObservedFrom()
	{
		return origin;
	}

	XY getLocation()
	{
		return location;
	}

	/**
	 * distance between observedFrom and location... implies accuracy
	 * 
	 * @return
	 */
	public Distance getDistance()
	{
		return distance;
	}

	public double getAccuracy()
	{
		return accuracy;
	}

	public double getX()
	{
		return location.getX();
	}

	public double getY()
	{
		return location.getY();
	}

	public Vector3D getVector()
	{
		return new Vector3D(location.getX(),location.getY());
	}

}
