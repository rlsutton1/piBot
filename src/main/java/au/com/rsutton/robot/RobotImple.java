package au.com.rsutton.robot;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import au.com.rsutton.hazelcast.LidarScan;
import au.com.rsutton.hazelcast.SetMotion;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.AngleUnits;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;
import au.com.rsutton.units.Speed;
import au.com.rsutton.units.Time;

public class RobotImple implements RobotInterface
{

	Angle steeringAngle = new Angle(0, AngleUnits.DEGREES);
	Speed speed = new Speed(new Distance(0, DistanceUnit.CM), Time.perSecond());
	private boolean freeze;

	public RobotImple()
	{
		new RobotLocationDeltaMessagePump(new RobotLocationDeltaListener()
		{

			@Override
			public void onMessage(Angle deltaHeading, Distance deltaDistance, boolean bump,
					Distance absoluteTotalDistance)
			{
				for (RobotLocationDeltaListener listener : listeners)
				{
					try
					{
						listener.onMessage(deltaHeading, deltaDistance, bump, absoluteTotalDistance);
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onMessage(LidarScan scan)
			{
				for (RobotLocationDeltaListener listener : listeners)
				{
					try
					{
						listener.onMessage(scan);
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
	public void setSteeringAngle(Angle steeringAngle)
	{
		this.steeringAngle = steeringAngle;

	}

	@Override
	public void publishUpdate()
	{
		SetMotion motion = new SetMotion();
		motion.setFreeze(freeze);
		motion.setSpeed(speed);
		motion.setSteeringAngle(steeringAngle);
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

	@Override
	public double getPlatformRadius()
	{
		// roomba
		return 15;
	}

}
