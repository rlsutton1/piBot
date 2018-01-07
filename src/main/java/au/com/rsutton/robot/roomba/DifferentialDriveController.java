package au.com.rsutton.robot.roomba;

import au.com.rsutton.units.Distance;
import au.com.rsutton.units.Speed;

public interface DifferentialDriveController
{

	void setSpeed(Speed leftSpeed, Speed rightSpeed);

	Distance getDistanceLeftWheel();

	Distance getDistanceRightWheel();

	double getDistanceBetweenWheels();

}
