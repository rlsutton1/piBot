package au.com.rsutton.robot;

import au.com.rsutton.units.Angle;
import au.com.rsutton.units.Speed;

public interface RobotInterface
{

	void freeze(boolean b);

	void setSpeed(Speed speed);

	void setSteeringAngle(Angle normalizeHeading);

	void publishUpdate();

	void addMessageListener(RobotLocationDeltaListener listener);

	void removeMessageListener(RobotLocationDeltaListener listener);

	double getPlatformRadius();

}
