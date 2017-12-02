package au.com.rsutton.robot.rover5;

import au.com.rsutton.units.Distance;
import au.com.rsutton.units.Speed;

public interface SingleWheelController
{

	void setSpeed(Speed speed);

	Distance getDistance();

}