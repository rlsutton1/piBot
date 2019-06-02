package au.com.rsutton.robot.lidar;

import java.io.Serializable;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.mapping.particleFilter.ScanObservation;

public class LidarObservation implements Serializable, ScanObservation
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Vector3D vector;
	private Double angleRadians;

	public LidarObservation(Vector3D vector)
	{
		this.vector = vector;
	}

	@Override
	public int getX()
	{
		return (int) vector.getX();
	}

	@Override
	public int getY()
	{
		return (int) vector.getY();
	}

	@Override
	public Vector3D getVector()
	{
		return vector;
	}

	@Override
	public double getDisctanceCm()
	{
		return Vector3D.distance(Vector3D.ZERO, vector);
	}

	@Override
	public double getAngleRadians()
	{
		if (angleRadians == null)
		{
			angleRadians = Math.atan2(vector.getY(), vector.getX()) - (Math.PI / 2.0);
		}
		return angleRadians;
	}

}
