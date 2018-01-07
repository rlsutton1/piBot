package au.com.rsutton.robot.rover;

import com.pi4j.gpio.extension.adafruit.GyroProvider;
import com.pi4j.gpio.extension.lsm303.HeadingData;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.robot.roomba.DifferentialDriveController;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.AngleUnits;
import au.com.rsutton.units.Distance;

public class GyroDeadReconing implements TelemetrySource
{

	private Angle heading;

	private final Object sync = new Object();

	private GyroProvider gyro;

	private double lastGyroHeading;

	public GyroDeadReconing(Angle angle, GyroProvider gyro)
	{
		heading = angle;
		this.gyro = gyro;
	}

	public void updateLocation(DifferentialDriveController wheels)
	{

		try
		{

			synchronized (sync)
			{
				// get gyro delta and prepare for the next
				double gyroHeading = gyro.getHeading();
				double gyroDelta = lastGyroHeading - gyroHeading;

				lastGyroHeading = gyroHeading;

				// determine heading allowing for slippage
				final double telemetryHeading = HeadingHelper.normalizeHeading(heading.getDegrees() - gyroDelta);

				heading = new Angle(telemetryHeading, AngleUnits.DEGREES);

				System.out.println("final " + heading.getDegrees());

			}

		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public Distance getTotalDistanceTravelled()
	{
		return null;
	}

	@Override
	public HeadingData getHeading()
	{
		synchronized (sync)
		{
			return new HeadingData((float) heading.getDegrees(), 0);
		}
	}

}
