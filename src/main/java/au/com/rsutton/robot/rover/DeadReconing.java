package au.com.rsutton.robot.rover;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;

import com.pi4j.gpio.extension.adafruit.GyroProvider;
import com.pi4j.gpio.extension.lsm303.HeadingData;

public class DeadReconing
{

	private static final int VEHICAL_WIDTH = 225;

	private final static DistanceUnit unit = DistanceUnit.MM;

	double initialX = 0;
	double initialY = 0;
	Angle heading;

	double initialLeftWheelReading = 0;
	double initialRightWheelReading = 0;

	double currentLeftWheelReading = 0;
	double currentRightWheelReading = 0;

	final private Object sync = new Object();

	private GyroProvider gyro;

	public DeadReconing(Angle angle, GyroProvider gyro)
	{
		heading = angle;
		this.gyro = gyro;
	}

	public void updateLocation(Distance leftDistance, Distance rightDistance, final HeadingData compassData)
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

				initialX += -Math.sin(heading.getRadians()) * ((t1 + t2) / 2.0d);
				initialY += -Math.cos(heading.getRadians()) * ((t1 + t2) / 2.0d);

				initialLeftWheelReading = currentLeftWheelReading;
				initialRightWheelReading = currentRightWheelReading;

				int gyroHeading = gyro.getHeading();

				if (Math.abs(0 - Math.toDegrees((t2 - t1) / VEHICAL_WIDTH)) > 0.1)
				{
					final double telemetryHeading = HeadingHelper.normalizeHeading(gyroHeading);

					heading = new Angle(telemetryHeading, AngleUnits.DEGREES);

				}

				System.out.println("final " + heading.getDegrees());
				System.out.println();

			}

		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public void replacement_updateLocation(Distance leftDistance, Distance rightDistance, final HeadingData compassData)
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

				double leftDelta = currentLeftWheelReading - initialLeftWheelReading;
				double rightDelta = currentRightWheelReading - initialRightWheelReading;

				if (Math.abs(leftDelta - rightDelta) < 0.000005)
				{
					initialX += leftDelta * Math.cos(heading.getRadians());
					initialY += leftDelta * Math.sin(heading.getRadians());
				} else
				{
					double r = VEHICAL_WIDTH * (leftDelta + rightDelta) / (2.0 * (rightDelta - leftDelta));
					double wd = (rightDelta - leftDelta) / VEHICAL_WIDTH;

					initialX += r * Math.sin(wd + heading.getRadians()) - r * Math.sin(heading.getRadians());
					initialY -= r * Math.cos(wd + heading.getRadians()) + r * Math.cos(heading.getRadians());
					heading = new Angle(heading.getRadians() + wd, AngleUnits.RADIANS);
				}

				initialLeftWheelReading = currentLeftWheelReading;
				initialRightWheelReading = currentRightWheelReading;

				System.out.println("final " + heading.getDegrees());
				System.out.println();

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

	public HeadingData getHeading()
	{
		synchronized (sync)
		{
			return new HeadingData((float) heading.getDegrees(), 0);
		}
	}

	// public void setHeading(int heading2)
	// {
	// heading = heading2;
	//
	// }

}
