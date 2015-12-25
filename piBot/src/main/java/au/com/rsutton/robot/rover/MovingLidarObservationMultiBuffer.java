package au.com.rsutton.robot.rover;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.hazelcast.RobotLocation;

public class MovingLidarObservationMultiBuffer
{

	private static final int MAX_BUFERS = 3;
	List<MovingLidarObservationBuffer> buffers = new CopyOnWriteArrayList<>();

	MovingLidarObservationBuffer currentBuffer;

	public void addObservation(RobotLocation data)
	{

		for (LidarObservation observation : data.getObservations())
		{
			if (observation.isStartOfScan())
			{
				currentBuffer = new MovingLidarObservationBuffer();
				buffers.add(currentBuffer);
				if (buffers.size() > MAX_BUFERS)
				{
					buffers.remove(0);
				}
			}
			if (currentBuffer != null)
			{
				RobotLocation location = new RobotLocation();
				location.setHeading(data.getHeading());
				location.setX(data.getX());
				location.setY(data.getY());
				List<LidarObservation> observations = new LinkedList<>();
				observations.add(observation);
				
				location.addObservations(observations);
				currentBuffer.addLidarObservation(data);
			}
		}

	}
	
	public List<LidarObservation> getObservations(Vector3D translation, Rotation rotation)
	{
		List<LidarObservation> observations = new LinkedList<>();
		for (MovingLidarObservationBuffer buffer:buffers)
		{
			observations.addAll(buffer.getTranslatedObservations(rotation, translation));
		}
		return observations;
	}

}
