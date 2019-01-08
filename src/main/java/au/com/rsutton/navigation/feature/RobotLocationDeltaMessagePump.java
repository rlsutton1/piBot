package au.com.rsutton.navigation.feature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.hazelcast.DataLogValue;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.AngleUnits;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;

public class RobotLocationDeltaMessagePump implements MessageListener<RobotLocation>
{

	Double lastHeading = null;
	private Distance lastDistance = null;
	private RobotLocationDeltaListener listener;

	Logger logger = LogManager.getLogger();

	public RobotLocationDeltaMessagePump(RobotLocationDeltaListener listener)
	{
		this.listener = listener;
		RobotLocation locationMessage = new RobotLocation();
		locationMessage.addMessageListener(this);
	}

	@Override
	public void onMessage(Message<RobotLocation> message)
	{
		try
		{
			RobotLocation robotLocation = message.getMessageObject();
			onMessage(robotLocation);
		} catch (Exception e)
		{
			logger.error(e, e);
		}
	}

	public void onMessage(RobotLocation robotLocation)
	{
		try
		{
			if (robotLocation.getTime() > System.currentTimeMillis() - 450)
			{

				Angle angle = robotLocation.getDeadReaconingHeading();
				if (lastHeading == null)
				{
					lastHeading = angle.getDegrees();
				}
				if (lastDistance == null)
				{
					lastDistance = robotLocation.getDistanceTravelled();
				}

				double deltaHeading = HeadingHelper.getChangeInHeading(angle.getDegrees(), lastHeading);
				double deltaDistance = robotLocation.getDistanceTravelled().convert(DistanceUnit.MM)
						- lastDistance.convert(DistanceUnit.MM);

				lastHeading = angle.getDegrees();
				lastDistance = robotLocation.getDistanceTravelled();

				logger.debug("raw angle: " + angle.getDegrees() + " delta: " + deltaHeading);
				logger.debug("raw diatance: " + robotLocation.getDistanceTravelled().convert(DistanceUnit.MM)
						+ " delta: " + deltaDistance);

				new DataLogValue("Message Pump-raw angle", "" + angle.getDegrees()).publish();
				new DataLogValue("Message Pump-delta angle", "" + deltaHeading).publish();
				new DataLogValue("Message Pump-raw distance",
						"" + robotLocation.getDistanceTravelled().convert(DistanceUnit.MM)).publish();
				new DataLogValue("Message Pump-delta distance", "" + deltaDistance).publish();

				listener.onMessage(new Angle(deltaHeading, AngleUnits.DEGREES),
						new Distance(deltaDistance, DistanceUnit.MM), robotLocation.getObservations(),
						robotLocation.isBumpLeft() || robotLocation.isBumpRight());
				logger.warn("Recieved and dispatched RobotLocation");
			} else
			{
				logger.error("Discarded old RobotLocation message "
						+ Math.abs(robotLocation.getTime() - System.currentTimeMillis()));
			}
		} catch (Exception e)
		{
			logger.error(e, e);
		}

	}

}
