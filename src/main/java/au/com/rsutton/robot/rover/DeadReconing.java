package au.com.rsutton.robot.rover;

import com.pi4j.gpio.extension.adafruit.GyroProvider;
import com.pi4j.gpio.extension.lsm303.HeadingData;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;

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

	private double gyroSlip;

	private double lastGyroHeading;

	public DeadReconing(Angle angle, GyroProvider gyro)
	{
		heading = angle;
		this.gyro = gyro;
	}

	long lastTime = System.currentTimeMillis();

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

				long now = System.currentTimeMillis();
				double elapsedMillis = now - lastTime;
				lastTime = now;
				double fraction = elapsedMillis / 1000.0;

				// max change = 25 deg/sec
				double maxChange = 25 * fraction;

				// get gyro delta and prepare for the next
				double gyroHeading = gyro.getHeading();
				double deltaGyroHeading = gyroHeading - lastGyroHeading;
				lastGyroHeading = gyroHeading;

				// check change in heading, based on odometry
				final double odoHeadingChange = Math.toDegrees((t2 - t1) / VEHICAL_WIDTH) * 2.0;

				if (Math.abs(odoHeadingChange) < 0.2)
				{
					// stationary, any change in gyro heading is slippage
					gyroSlip += deltaGyroHeading;

				} else if (Math.abs(deltaGyroHeading) > maxChange)
				{
					// excessive change in gyro heading, must be slipapge
					gyroSlip += (Math.abs(deltaGyroHeading) - maxChange) * Math.signum(deltaGyroHeading);
				}

				// determine heading allowing for slippage
				final double telemetryHeading = HeadingHelper.normalizeHeading(gyroHeading - gyroSlip);

				heading = new Angle(telemetryHeading, AngleUnits.DEGREES);

				System.out.println(gyroHeading + " " + gyroSlip);
				System.out.println("final " + heading.getDegrees());

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
