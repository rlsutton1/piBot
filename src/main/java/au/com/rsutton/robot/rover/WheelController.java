package au.com.rsutton.robot.rover;

import au.com.rsutton.units.Distance;
import au.com.rsutton.units.Speed;

public interface WheelController
{

	void setSpeed(Speed leftSpeed, Speed rightSpeed);

	Distance getDistanceLeftWheel();

	Distance getDistanceRightWheel();

}
