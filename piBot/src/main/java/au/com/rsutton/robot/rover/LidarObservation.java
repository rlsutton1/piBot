package au.com.rsutton.robot.rover;

import java.io.Serializable;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class LidarObservation implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Vector3D vector;
	private double disctanceCm;
	private double angle;

	public LidarObservation()
	{
		
	}
	
	public LidarObservation(Vector3D vector, double distanceCm,
			double angleDegrees)
	{
		this.vector = vector;
		this.disctanceCm = distanceCm;
		this.angle = angle;
	}

	public int getX()
	{
		return (int) vector.getX();
	}

	public int getY()
	{
		return (int) vector.getY();
	}

}
