package au.com.rsutton.navigation.feature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.hazelcast.DataLogValue;
import au.com.rsutton.hazelcast.LidarScan;
import au.com.rsutton.hazelcast.RobotTelemetry;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.AngleUnits;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;

public class RobotLocationDeltaMessagePump
{

	Double lastHeading = null;
	private Distance lastDistance = null;
	private RobotLocationDeltaListener listener;

	Logger logger = LogManager.getLogger();

	public RobotLocationDeltaMessagePump(RobotLocationDeltaListener listener)
	{
		this.listener = listener;
		RobotTelemetry locationMessage = new RobotTelemetry();
		locationMessage.addMessageListener(getTelemetryListener());

		LidarScan lidarScan = new LidarScan();
		lidarScan.addMessageListener(getScanListener());
	}

	private MessageListener<LidarScan> getScanListener()
	{
		return new MessageListener<LidarScan>()
		{
			@Override
			public void onMessage(Message<LidarScan> message)
			{
				try
				{
					LidarScan robotLocation = message.getMessageObject();
					RobotLocationDeltaMessagePump.this.onMessage(robotLocation);
				} catch (Exception e)
				{
					logger.error(e, e);
				}
			}
		};
	}

	MessageListener<RobotTelemetry> getTelemetryListener()
	{

		return new MessageListener<RobotTelemetry>()
		{
			@Override
			public void onMessage(Message<RobotTelemetry> message)
			{
				try
				{
					RobotTelemetry robotLocation = message.getMessageObject();
					RobotLocationDeltaMessagePump.this.onMessage(robotLocation);
				} catch (Exception e)
				{
					logger.error(e, e);
				}
			}
		};
	}

	public void onMessage(RobotTelemetry telemetry)
	{
		try
		{
			if (telemetry.getTime() > System.currentTimeMillis() - 450)
			{

				Angle angle = telemetry.getDeadReaconingHeading();
				if (lastHeading == null)
				{
					lastHeading = angle.getDegrees();
				}
				if (lastDistance == null)
				{
					lastDistance = telemetry.getDistanceTravelled();
				}

				double deltaHeading = HeadingHelper.getChangeInHeading(angle.getDegrees(), lastHeading);
				double deltaDistance = telemetry.getDistanceTravelled().convert(DistanceUnit.MM)
						- lastDistance.convert(DistanceUnit.MM);

				lastHeading = angle.getDegrees();
				lastDistance = telemetry.getDistanceTravelled();

				logger.debug("raw angle: " + angle.getDegrees() + " delta: " + deltaHeading);
				logger.debug("raw diatance: " + telemetry.getDistanceTravelled().convert(DistanceUnit.MM) + " delta: "
						+ deltaDistance);

				new DataLogValue("Message Pump-raw angle", "" + angle.getDegrees()).publish();
				new DataLogValue("Message Pump-delta angle", "" + deltaHeading).publish();
				new DataLogValue("Message Pump-raw distance",
						"" + telemetry.getDistanceTravelled().convert(DistanceUnit.MM)).publish();
				new DataLogValue("Message Pump-delta distance", "" + deltaDistance).publish();

				listener.onMessage(new Angle(deltaHeading, AngleUnits.DEGREES),
						new Distance(deltaDistance, DistanceUnit.MM),
						telemetry.isBumpLeft() || telemetry.isBumpRight());
				logger.warn("Recieved and dispatched RobotLocation");
			} else
			{
				logger.error("Discarded old RobotLocation message "
						+ Math.abs(telemetry.getTime() - System.currentTimeMillis()));
			}
		} catch (Exception e)
		{
			logger.error(e, e);
		}

	}

	public void onMessage(LidarScan scan)
	{
		try
		{
			if (scan.getTime() > System.currentTimeMillis() - 450)
			{

				listener.onMessage(scan);
				logger.warn("Recieved and dispatched RobotLocation");
			} else
			{
				logger.error(
						"Discarded old RobotLocation message " + Math.abs(scan.getTime() - System.currentTimeMillis()));
			}
		} catch (Exception e)
		{
			logger.error(e, e);
		}

	}

}
