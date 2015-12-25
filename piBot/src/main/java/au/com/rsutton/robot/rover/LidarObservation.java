package au.com.rsutton.robot.rover;

import java.io.Serializable;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.hazelcast.HcTopic;
import au.com.rsutton.hazelcast.MessageBase;

public class LidarObservation extends MessageBase<LidarObservation> implements
		Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Vector3D vector;
	private boolean isStartOfScan = false;

	public LidarObservation()
	{
		super(HcTopic.LIDAR_OBSERVATION);
	}

	public LidarObservation(Vector3D vector,boolean isStartOfScan)
	{
		super(HcTopic.LIDAR_OBSERVATION);
		this.vector = vector;
		this.isStartOfScan = isStartOfScan;
	}

	public int getX()
	{
		return (int) vector.getX();
	}

	public int getY()
	{
		return (int) vector.getY();
	}

	public Vector3D getVector()
	{
		return vector;
	}

	public double getDisctanceCm()
	{
		return Vector3D.distance(Vector3D.ZERO, vector);
	}

	public boolean isStartOfScan()
	{
		return isStartOfScan ;
	}

}
