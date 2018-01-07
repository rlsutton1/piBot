package au.com.rsutton.robot.rover;

import com.pi4j.gpio.extension.lsm303.HeadingData;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.robot.roomba.DifferentialDriveController;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.AngleUnits;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;

public class DifferentialDriveDeadReconing implements TelemetrySource
{

	private static final DistanceUnit MILLIMETERS = DistanceUnit.MM;

	private double totalDistanceTravelled = 0;
	private Angle heading;

	private double initialLeftWheelReading = 0;
	private double initialRightWheelReading = 0;

	private double currentLeftWheelReading = 0;
	private double currentRightWheelReading = 0;

	private final Object sync = new Object();

	private DifferentialDriveController wheels;

	public DifferentialDriveDeadReconing(Angle angle, DifferentialDriveController wheels)
	{
		heading = angle;
		this.wheels = wheels;
	}

	public void updateLocation()
	{

		try
		{

			synchronized (sync)
			{
				currentLeftWheelReading = wheels.getDistanceLeftWheel().convert(MILLIMETERS);

				currentRightWheelReading = wheels.getDistanceRightWheel().convert(MILLIMETERS);

				double t1 = initialLeftWheelReading - currentLeftWheelReading;
				double t2 = initialRightWheelReading - currentRightWheelReading;

				initialLeftWheelReading = currentLeftWheelReading;
				initialRightWheelReading = currentRightWheelReading;

				// check change in heading, based on odometry
				final double delta = Math.toDegrees((t2 - t1) / wheels.getDistanceBetweenWheels());

				// determine heading allowing for slippage
				final double telemetryHeading = HeadingHelper.normalizeHeading(heading.getDegrees() + delta);

				totalDistanceTravelled += (t1 + t2) / 2.0d;
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
		synchronized (sync)
		{
			return new Distance(totalDistanceTravelled, MILLIMETERS);
		}
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
