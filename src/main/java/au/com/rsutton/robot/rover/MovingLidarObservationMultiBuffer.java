package au.com.rsutton.robot.rover;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.mapping.particleFilter.ScanObservation;

public class MovingLidarObservationMultiBuffer
{

	private final int MAX_BUFERS;
	List<MovingLidarObservationBuffer> buffers = new CopyOnWriteArrayList<>();

	final AtomicReference<MovingLidarObservationBuffer> currentBuffer = new AtomicReference<>();

	public MovingLidarObservationMultiBuffer(int maxBuffers)
	{
		MAX_BUFERS = maxBuffers;
		currentBuffer.set(new MovingLidarObservationBuffer());
		buffers.add(currentBuffer.get());
	}

	public void addObservation(RobotLocation data)
	{

		for (ScanObservation observation : data.getObservations())
		{
			// if (observation.isStartOfScan())
			// {
			currentBuffer.set(new MovingLidarObservationBuffer());
			buffers.add(currentBuffer.get());
			if (buffers.size() > MAX_BUFERS)
			{
				buffers.remove(0);
			}
			// }

			currentBuffer.get().addLidarObservation(data);

		}

	}

	public List<LidarObservation> getObservations(RobotLocation data)
	{
		List<LidarObservation> observations = new LinkedList<>();
		for (MovingLidarObservationBuffer buffer : buffers)
		{
			observations.addAll(buffer.getTranslatedObservations(data));
		}
		return observations;
	}

}
