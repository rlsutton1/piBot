package au.com.rsutton.robot.roomba;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.pi4j.gpio.extension.lsm303.HeadingData;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.robot.rover.TelemetrySource;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.AngleUnits;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;

public class DeadReconing implements TelemetrySource
{

	private static final DistanceUnit MILLIMETERS = DistanceUnit.MM;

	private double totalDistanceTravelled = 0;
	private Angle heading;

	private final Object sync = new Object();

	private double totalError;

	TelemetrySource telemetrySources[];

	Distance lastDistance[];
	HeadingData lastHeading[];

	public DeadReconing(TelemetrySource telemetrySources[])
	{
		this.telemetrySources = telemetrySources;
		for (int i = 0; i < telemetrySources.length; i++)
		{
			lastDistance[i] = telemetrySources[i].getTotalDistanceTravelled();
			lastHeading[i] = telemetrySources[i].getHeading();
		}
	}

	public void updateLocation()
	{

		try
		{

			synchronized (sync)
			{

				double travelCount = 0;
				double headingCount = 0;
				double travelled = 0;

				Vector3D unit = new Vector3D(0, 1, 0);

				Vector3D angle = new Vector3D(0, 0, 0);

				for (int i = 0; i < telemetrySources.length; i++)
				{
					Distance sourceTravelled = telemetrySources[i].getTotalDistanceTravelled();
					if (sourceTravelled != null)
					{
						travelCount++;
						travelled += sourceTravelled.convert(MILLIMETERS);
						lastDistance[i] = sourceTravelled;

					}
					HeadingData sourceHeading = telemetrySources[i].getHeading();
					if (sourceHeading != null)
					{
						headingCount++;
						double changeDegrees = HeadingHelper.getChangeInHeading(sourceHeading.getHeading(),
								lastHeading[i].getHeading());

						Rotation r1 = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(changeDegrees));

						angle.add(r1.applyTo(unit));

						lastHeading[i] = sourceHeading;
					}
				}

				totalDistanceTravelled += (travelled / travelCount);

				double newAngle = HeadingHelper
						.normalizeHeading(heading.getDegrees() + Math.toDegrees(Vector3D.angle(unit, angle)));
				heading = new Angle(newAngle, AngleUnits.DEGREES);

				// the vector length / the expected vector length
				double headingCertainty = angle.getNorm() / headingCount;

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
