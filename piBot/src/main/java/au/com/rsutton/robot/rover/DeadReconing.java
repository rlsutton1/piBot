package au.com.rsutton.robot.rover;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;

public class DeadReconing
{

	private final static DistanceUnit unit = DistanceUnit.MM;

	double initialX = 0;
	double initialY = 0;
	double heading;
	double initialLeftWheelReading = 0;
	double initialRightWheelReading = 0;

	double currentLeftWheelReading = 0;
	double currentRightWheelReading = 0;

	public void updateLocation(Distance leftDistance, Distance rightDistance)
	{

		try
		{

			if (leftDistance != null)
			{
				currentLeftWheelReading = leftDistance.convert(unit);
			}
			if (rightDistance != null)
			{
				currentRightWheelReading = rightDistance.convert(unit);
			}

			double t1 = initialLeftWheelReading - currentLeftWheelReading;
			double t2 = initialRightWheelReading - currentRightWheelReading;

			initialX += -Math.sin(Math.toRadians(heading)) * ((t1 + t2) / 2.0d);
			initialY += -Math.cos(Math.toRadians(heading)) * ((t1 + t2) / 2.0d);

			initialLeftWheelReading = currentLeftWheelReading;
			initialRightWheelReading = currentRightWheelReading;

		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public Distance getX()
	{

		return new Distance(initialX, unit);
	}

	public Distance getY()
	{

		return new Distance(initialY, unit);

	}

	public void setHeading(int heading2)
	{
		heading = heading2;

	}

}
