package au.com.rsutton.hazelcast;

import java.io.Serializable;

import com.hazelcast.core.ITopic;
import com.hazelcast.core.MessageListener;

public abstract class MessageBase<M> implements Serializable
{

	
	private static final long serialVersionUID = 1130437880736641457L;
	protected transient ITopic<M> topic;

	MessageBase(HcTopic topic)
	{
		this.topic = HazelCastInstance.getInstance().getTopic(topic.toString());
	}

	@SuppressWarnings("unchecked")
	public void publish()
	{
		topic.publish((M) this);
	}

	public void addMessageListener(MessageListener<M> listener)
	{
		topic.addMessageListener(listener);
	}
}
