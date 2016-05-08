package au.com.rsutton.robot.rover;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import au.com.rsutton.hazelcast.RobotLocation;

public class MovingLidarObservationMultiBuffer
{

	private final  int MAX_BUFERS;
	List<MovingLidarObservationBuffer> buffers = new CopyOnWriteArrayList<>();

	MovingLidarObservationBuffer currentBuffer;

	public MovingLidarObservationMultiBuffer(int maxBuffers)
	{
		MAX_BUFERS = maxBuffers;
	}

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
				currentBuffer.addLidarObservation(data);
			}
		}

	}
	
	public List<LidarObservation> getObservations(RobotLocation data)
	{
		List<LidarObservation> observations = new LinkedList<>();
		for (MovingLidarObservationBuffer buffer:buffers)
		{
			observations.addAll(buffer.getTranslatedObservations(data));
		}
		return observations;
	}

}
