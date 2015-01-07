package au.com.rsutton.mapping;

import java.util.LinkedList;
import java.util.List;

import au.com.rsutton.hazelcast.RobotLocation;

public class FrameJoiner
{

	RobotLocation lastFrame = null;

	public void join(RobotLocation messageObject, Graph graph)
	{
		List<RobotLocation> frames = new LinkedList<>();

		if (lastFrame == null)
		{
			lastFrame = messageObject;
			return;
		}
		if (messageObject.getTime() - lastFrame.getTime() < 100)
		{
			// merge frames
			frames.add(lastFrame);
			frames.add(messageObject);
			lastFrame = null;
			graph.handlePointCloud(frames);
		} else
		{
			frames.add(lastFrame);
			graph.handlePointCloud(frames);
			lastFrame = messageObject;
		}

	}

}
