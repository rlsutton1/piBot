package au.com.rsutton.mapping.particleFilter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import au.com.rsutton.hazelcast.SetMotion;
import au.com.rsutton.navigation.feature.RobotLocationDeltaListener;
import au.com.rsutton.navigation.feature.RobotLocationDeltaMessagePump;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;
import au.com.rsutton.units.Speed;
import au.com.rsutton.units.Time;

public class RobotImple implements RobotInterface
{

	double heading = 0;
	Speed speed = new Speed(new Distance(0, DistanceUnit.CM), Time.perSecond());
	private boolean freeze;

	public RobotImple()
	{
		RobotLocationDeltaMessagePump robotDataBus = new RobotLocationDeltaMessagePump(new RobotLocationDeltaListener()
		{

			@Override
			public void onMessage(Angle deltaHeading, Distance deltaDistance, List<ScanObservation> robotLocation,
					boolean bump)
			{
				for (RobotLocationDeltaListener listener : listeners)
				{
					try
					{
						listener.onMessage(deltaHeading, deltaDistance, robotLocation, bump);
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
	public void turn(double normalizeHeading)
	{
		this.heading = normalizeHeading;

	}

	@Override
	public void publishUpdate()
	{
		SetMotion motion = new SetMotion();
		motion.setFreeze(freeze);
		motion.setSpeed(speed);
		motion.setChangeHeading(heading);
		motion.publish();

	}

	@Override
	public void freeze(boolean b)
	{
		freeze = b;

	}

	List<RobotLocationDeltaListener> listeners = new CopyOnWriteArrayList<>();

	@Override
	public void addMessageListener(final RobotLocationDeltaListener listener)
	{
		listeners.add(listener);

	}

	@Override
	public void removeMessageListener(RobotLocationDeltaListener listener)
	{
		listeners.remove(listener);
	}
}
