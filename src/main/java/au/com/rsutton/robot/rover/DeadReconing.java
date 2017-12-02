package au.com.rsutton.robot.rover;

import com.pi4j.gpio.extension.adafruit.GyroProvider;
import com.pi4j.gpio.extension.lsm303.HeadingData;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.AngleUnits;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;

public class DeadReconing
{

	private static final double VEHICAL_WIDTH = 220;

	private static final DistanceUnit MILLIMETERS = DistanceUnit.MM;

	private double totalDistanceTravelled = 0;
	private Angle heading;

	private double initialLeftWheelReading = 0;
	private double initialRightWheelReading = 0;

	private double currentLeftWheelReading = 0;
	private double currentRightWheelReading = 0;

	private final Object sync = new Object();

	private GyroProvider gyro;

	private double lastGyroHeading;

	private double totalError;

	public DeadReconing(Angle angle, GyroProvider gyro)
	{
		heading = angle;
		this.gyro = gyro;
	}

	public void updateLocation(WheelController wheels)
	{

		try
		{

			synchronized (sync)
			{
				currentLeftWheelReading = wheels.getDistanceLeftWheel().convert(MILLIMETERS);

				currentRightWheelReading = wheels.getDistanceRightWheel().convert(MILLIMETERS);

				double t1 = initialLeftWheelReading - currentLeftWheelReading;
				double t2 = initialRightWheelReading - currentRightWheelReading;

				totalDistanceTravelled += (t1 + t2) / 2.0d;

				initialLeftWheelReading = currentLeftWheelReading;
				initialRightWheelReading = currentRightWheelReading;

				// get gyro delta and prepare for the next
				double gyroHeading = gyro.getHeading();
				double gyroDelta = lastGyroHeading - gyroHeading;

				// flip gyro direction
				gyroDelta = -1.0 * gyroDelta;
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

				totalError += error;

				// determine heading allowing for slippage
				final double telemetryHeading = HeadingHelper.normalizeHeading(heading.getDegrees() - delta);

				heading = new Angle(telemetryHeading, AngleUnits.DEGREES);

				System.out.println("gyro: " + gyroDelta + " odo: " + odoDelta + " err: " + error);
				System.out.println("final " + heading.getDegrees() + " total Error " + totalError);

			}

		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public Distance getTotalDistanceTravelled()
	{
		synchronized (sync)
		{
			return new Distance(totalDistanceTravelled, MILLIMETERS);
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
