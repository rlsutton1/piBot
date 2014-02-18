package au.com.rsutton.robot;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;

public class DeadReconingWithGyro
{

	private final static DistanceUnit unit = DistanceUnit.MM;

	double initialX = 0;
	double initialY = 0;
	int heading;
	double initialLeftWheelReading = 0;
	double initialRightWheelReading = 0;

	double currentLeftWheelReading = 0;
	double currentRightWheelReading = 0;

	public synchronized void resetLocation(Distance x, Distance y, int heading)
	{
		initialX = x.convert(unit);
		initialY = y.convert(unit);
		initialLeftWheelReading = currentLeftWheelReading;
		initialRightWheelReading = currentRightWheelReading;
		this.heading = heading;
	}

	public synchronized void updateLocation(Distance distance,
			Distance distance2, int heading)
	{
		if (heading != this.heading)
		{
			resetLocation(getX(), getY(), heading);
		}
		if (distance != null)
		{
			currentLeftWheelReading = distance.convert(unit);
		}
		if (distance2 != null)
		{
			currentRightWheelReading = distance2.convert(unit);
		}

	}

	public synchronized Distance getX()
	{

		double distance = ((initialLeftWheelReading - currentLeftWheelReading) + (initialRightWheelReading - currentRightWheelReading)) / 2d;
		return new Distance(initialX
				+ (Math.sin(Math.toRadians(heading)) * distance), unit);
	}

	public synchronized Distance getY()
	{
		double distance = ((initialLeftWheelReading - currentLeftWheelReading) + (initialRightWheelReading - currentRightWheelReading)) / 2d;
		return new Distance(initialY
				- (Math.cos(Math.toRadians(heading)) * distance), unit);

	}

	public synchronized int getHeading()
	{

		return heading;
	}

}
