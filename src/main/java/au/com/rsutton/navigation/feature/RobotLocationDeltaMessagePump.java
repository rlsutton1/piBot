package au.com.rsutton.navigation.feature;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.robot.rover.Angle;
import au.com.rsutton.robot.rover.AngleUnits;

public class RobotLocationDeltaMessagePump implements MessageListener<RobotLocation>
{

	Double lastHeading = null;
	private Distance lastDistance = null;
	private RobotLocationDeltaListener listener;

	public RobotLocationDeltaMessagePump(RobotLocationDeltaListener listener)
	{
		this.listener = listener;
		RobotLocation locationMessage = new RobotLocation();
		locationMessage.addMessageListener(this);
	}

	@Override
	public void onMessage(Message<RobotLocation> message)
	{
		RobotLocation robotLocation = message.getMessageObject();

		Angle angle = robotLocation.getDeadReaconingHeading();
		if (lastHeading == null)
		{
			lastHeading = angle.getDegrees();
		}
		if (lastDistance == null)
		{
			lastDistance = robotLocation.getDistanceTravelled();
		}

		double deltaHeading = angle.getDegrees() - lastHeading;
		double deltaDistance = lastDistance.convert(DistanceUnit.MM)
				- robotLocation.getDistanceTravelled().convert(DistanceUnit.MM);

		lastHeading = angle.getDegrees();
		lastDistance = robotLocation.getDistanceTravelled();

		listener.onMessage(new Angle(deltaHeading, AngleUnits.DEGREES), new Distance(deltaDistance, DistanceUnit.MM),
				robotLocation.getObservations());

	}

}
