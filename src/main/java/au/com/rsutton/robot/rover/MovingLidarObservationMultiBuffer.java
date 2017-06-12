package au.com.rsutton.robot.rover;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import au.com.rsutton.mapping.particleFilter.RobotPoseSource;
import au.com.rsutton.mapping.particleFilter.ScanObservation;
import au.com.rsutton.robot.RobotInterface;

public class MovingLidarObservationMultiBuffer
{

	private final int MAX_BUFERS;
	List<MovingLidarObservationBuffer> buffers = new CopyOnWriteArrayList<>();

	final AtomicReference<MovingLidarObservationBuffer> currentBuffer = new AtomicReference<>();
	private RobotPoseSource poseSource;

	public MovingLidarObservationMultiBuffer(int maxBuffers, RobotInterface robot, RobotPoseSource pf)
	{
		this.poseSource = pf;// new RobotPoseSourceDeadReconning(robot);
		MAX_BUFERS = maxBuffers;
		currentBuffer.set(new MovingLidarObservationBuffer(poseSource));
		buffers.add(currentBuffer.get());
	}

	public void addObservation(List<ScanObservation> data)
	{

		currentBuffer.set(new MovingLidarObservationBuffer(poseSource));
		buffers.add(currentBuffer.get());
		if (buffers.size() > MAX_BUFERS)
		{
			buffers.remove(0);
		}

		currentBuffer.get().addLidarObservation(data);

	}

	public List<LidarObservation> getObservations()
	{
		List<LidarObservation> observations = new LinkedList<>();
		for (MovingLidarObservationBuffer buffer : buffers)
		{
			observations.addAll(buffer.getTranslatedObservations());
		}
		return observations;
	}

}
