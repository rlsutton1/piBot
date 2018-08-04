package au.com.rsutton.robot;

import au.com.rsutton.navigation.feature.RobotLocationDeltaListener;
import au.com.rsutton.units.Speed;

public interface RobotInterface
{

	void freeze(boolean b);

	void setSpeed(Speed speed);

	void turn(double normalizeHeading);

	void publishUpdate();

	void addMessageListener(RobotLocationDeltaListener listener);

	void removeMessageListener(RobotLocationDeltaListener listener);

	double getRadius();

}
