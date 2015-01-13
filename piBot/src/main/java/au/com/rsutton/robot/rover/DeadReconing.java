package au.com.rsutton.robot.rover;

import java.util.LinkedList;
import java.util.List;

import au.com.rsutton.entryPoint.trig.TrigMath;
import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;

public class DeadReconing
{

	private static final int VEHICAL_WIDTH = 250;

	private final static DistanceUnit unit = DistanceUnit.MM;

	double initialX = 0;
	double initialY = 0;
	Angle heading;
	double initialLeftWheelReading = 0;
	double initialRightWheelReading = 0;

	double currentLeftWheelReading = 0;
	double currentRightWheelReading = 0;

	private Angle headingError;

	final private Object sync = new Object();

	public DeadReconing(Angle angle)
	{
		heading = angle;
	}

	public void updateLocation(Distance leftDistance, Distance rightDistance,
			Angle angle)
	{

		try
		{

			synchronized (sync)
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

				initialX += -Math.sin(heading.getRadians())
						* ((t1 + t2) / 2.0d);
				initialY += -Math.cos(heading.getRadians())
						* ((t1 + t2) / 2.0d);

				initialLeftWheelReading = currentLeftWheelReading;
				initialRightWheelReading = currentRightWheelReading;

				double changeInHeading = -Math.toDegrees((t2 - t1)
						/ VEHICAL_WIDTH);
				System.out.println("Compass: " + angle.getDegrees()
						+ " heading: " + heading.getDegrees()
						+ " changeInHeading: " + changeInHeading);
				heading = heading.add(changeInHeading, AngleUnits.DEGREES);

				// get error between compass heading and deadReconing heading
				headingError = new Angle(heading.difference(angle.getDegrees(),
						AngleUnits.DEGREES), AngleUnits.DEGREES);

				int proportioning = 20;
				if (Math.abs(headingError.getDegrees())>6)
				{
					System.out.println("Closing heading gap...");
					proportioning = 5;
				}
				// average with compass heading
				List<Double> angles = new LinkedList<>();
				angles.add(angle.getDegrees());
				for (int i = 0; i < proportioning; i++)
				{
					angles.add(heading.getDegrees());
				}
				heading = new Angle(TrigMath.averageAngles(angles),
						AngleUnits.DEGREES);

				// get error between compass heading and deadReconing heading
				headingError = new Angle(heading.difference(angle.getDegrees(),
						AngleUnits.DEGREES), AngleUnits.DEGREES);


			}

		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public Distance getX()
	{

		synchronized (sync)
		{
			return new Distance(initialX, unit);
		}
	}

	public Distance getY()
	{
		synchronized (sync)
		{
			return new Distance(initialY, unit);
		}

	}

	public Angle getHeading()
	{
		synchronized (sync)
		{
			return heading;
		}
	}

	public Angle getHeadingError()
	{
		synchronized (sync)
		{
			return headingError;
		}
	}

	// public void setHeading(int heading2)
	// {
	// heading = heading2;
	//
	// }

}
