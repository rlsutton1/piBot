package au.com.rsutton.robot.rover;

import com.pi4j.gpio.extension.adafruit.GyroProvider;
import com.pi4j.gpio.extension.lsm303.HeadingData;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;

public class DeadReconing
{

	private static final double VEHICAL_WIDTH = 185;

	private final static DistanceUnit MILLIMETERS = DistanceUnit.MM;

	double initialX = 0;
	double initialY = 0;
	Angle heading;

	double initialLeftWheelReading = 0;
	double initialRightWheelReading = 0;

	double currentLeftWheelReading = 0;
	double currentRightWheelReading = 0;

	final private Object sync = new Object();

	private GyroProvider gyro;

	private double lastGyroHeading;

	public DeadReconing(Angle angle, GyroProvider gyro)
	{
		heading = angle;
		this.gyro = gyro;
	}

	public void updateLocation(Distance leftDistance, Distance rightDistance)
	{

		try
		{

			synchronized (sync)
			{
				if (leftDistance != null)
				{
					currentLeftWheelReading = leftDistance.convert(MILLIMETERS);
				}
				if (rightDistance != null)
				{
					currentRightWheelReading = rightDistance.convert(MILLIMETERS);
				}

				double t1 = initialLeftWheelReading - currentLeftWheelReading;
				double t2 = initialRightWheelReading - currentRightWheelReading;

				initialX += -Math.sin(heading.getRadians()) * ((t1 + t2) / 2.0d);
				initialY += -Math.cos(heading.getRadians()) * ((t1 + t2) / 2.0d);

				initialLeftWheelReading = currentLeftWheelReading;
				initialRightWheelReading = currentRightWheelReading;

				// get gyro delta and prepare for the next
				double gyroHeading = gyro.getHeading();
				double gyroDelta = gyroHeading - lastGyroHeading;
				lastGyroHeading = gyroHeading;

				// check change in heading, based on odometry
				final double odoDelta = Math.toDegrees((t2 - t1) / VEHICAL_WIDTH);

				if (Math.signum(gyroDelta) != Math.signum(odoDelta))
				{
					System.out.println("Gyro and odo are different directions");
				}

				// cap the gyro delta equal to the odo delta and use the
				// direction of the odo
				gyroDelta = Math.min(Math.abs(gyroDelta), Math.abs(odoDelta)) * Math.signum(odoDelta);

				// average the gyro and odo data

				double delta = (odoDelta * 0.5) + (gyroDelta * 0.5);
				double error = Math.abs(odoDelta - gyroDelta);

				// determine heading allowing for slippage
				final double telemetryHeading = HeadingHelper.normalizeHeading(heading.getDegrees() - delta);

				heading = new Angle(telemetryHeading, AngleUnits.DEGREES);

				System.out.println(gyroDelta + " " + odoDelta);
				System.out.println("final " + heading.getDegrees());

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
			return new Distance(initialX, MILLIMETERS);
		}
	}

	public Distance getY()
	{
		synchronized (sync)
		{
			return new Distance(initialY, MILLIMETERS);
		}

	}

	public HeadingData getHeading()
	{
		synchronized (sync)
		{
			return new HeadingData((float) heading.getDegrees(), 0);
		}
	}

}
