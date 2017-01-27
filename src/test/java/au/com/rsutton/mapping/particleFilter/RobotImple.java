package au.com.rsutton.mapping.particleFilter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.hazelcast.SetMotion;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.robot.RobotListener;

public class RobotImple implements RobotInterface
{

	double heading = 0;
	Speed speed = new Speed(new Distance(0, DistanceUnit.CM), Time.perSecond());
	private boolean freeze;

	public RobotImple()
	{
		RobotLocation robotDataBus = new RobotLocation();
		robotDataBus.addMessageListener(new MessageListener<RobotLocation>()
		{

			@Override
			public void onMessage(Message<RobotLocation> message)
			{
				System.out.println("Message received ");
				for (RobotListener listener : listeners)
				{
					try
					{
						listener.observed(message.getMessageObject());
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		});
	}

	@Override
	public void setSpeed(Speed speed)
	{
		this.speed = speed;

	}

	@Override
	public void setHeading(double normalizeHeading)
	{
		this.heading = normalizeHeading;

	}

	@Override
	public void publishUpdate()
	{
		SetMotion motion = new SetMotion();
		motion.setFreeze(freeze);
		motion.setSpeed(speed);
		motion.setHeading(heading);
		motion.publish();

	}

	@Override
	public void freeze(boolean b)
	{
		freeze = b;

	}

	List<RobotListener> listeners = new CopyOnWriteArrayList<>();

	@Override
	public void addMessageListener(final RobotListener listener)
	{
		listeners.add(listener);

	}

	@Override
	public void removeMessageListener(RobotListener listener)
	{
		listeners.remove(listener);
	}
}
