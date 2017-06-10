package au.com.rsutton.robot;

import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.navigation.feature.RobotLocationDeltaListener;

public interface RobotInterface
{

	void freeze(boolean b);

	void setSpeed(Speed speed);

	void turn(double normalizeHeading);

	void publishUpdate();

	void addMessageListener(RobotLocationDeltaListener listener);

	void removeMessageListener(RobotLocationDeltaListener listener);

}
