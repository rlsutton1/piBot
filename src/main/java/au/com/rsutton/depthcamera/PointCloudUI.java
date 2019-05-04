package au.com.rsutton.depthcamera;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import au.com.rsutton.hazelcast.PointCloudMessage;
import au.com.rsutton.mapping.particleFilter.RobotPoseSource;

public class PointCloudUI implements MessageListener<PointCloudMessage>
{

	private final AtomicReference<List<Vector3D>> vectors = new AtomicReference<>();
	private RobotPoseSource pf;

	public PointCloudUI(RobotPoseSource pf)
	{
		this.pf = pf;
		new PointCloudMessage().addMessageListener(this);
		vectors.set(new LinkedList<>());
	}

	@Override
	public void onMessage(Message<PointCloudMessage> message)
	{
		vectors.set(message.getMessageObject().getPoints());

	}
}
