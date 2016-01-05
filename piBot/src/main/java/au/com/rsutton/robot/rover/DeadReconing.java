package au.com.rsutton.robot.rover;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;

import com.pi4j.gpio.extension.lsm303.HeadingData;

public class DeadReconing
{

	private static final float MIN_TELEMETRY_ERROR_DEGREES = 0.1f;

	private static final int VEHICAL_WIDTH = 200;

	private final static DistanceUnit unit = DistanceUnit.MM;

	double initialX = 0;
	double initialY = 0;
	Angle heading;

	double headingError = 0;

	double initialLeftWheelReading = 0;
	double initialRightWheelReading = 0;

	double currentLeftWheelReading = 0;
	double currentRightWheelReading = 0;

	final KalmanFilter kalmanFilter;

	final private Object sync = new Object();

	public DeadReconing(Angle angle)
	{
		heading = angle;
		kalmanFilter = new KalmanFilter(new KalmanValue(angle.getDegrees(), 0.1));
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

				final double telemetryHeading = heading.getDegrees() - Math.toDegrees((t2 - t1) / VEHICAL_WIDTH);

				final double telemtryError = Math.max(0.1, Math.abs(compassData.getHeading() - telemetryHeading)
						- compassData.getError());
				System.out.println("Telemetry heading " + (int) telemetryHeading + " " + telemtryError);
				System.out.println("Compass heading " + (int) +compassData.getHeading() + " " + compassData.getError());

				kalmanFilter.calculate(new KalmanDataProvider()
				{

					@Override
					public KalmanValue getObservation()
					{
						// TODO Auto-generated method stub
						return new KalmanValue(compassData.getHeading(), compassData.getError());
					}

					@Override
					public KalmanValue getCalculatedNewValue(KalmanValue previousValue)
					{
						return new KalmanValue(telemetryHeading, telemtryError);
					}
				});

				heading = new Angle(kalmanFilter.getCurrentValue().getEstimate(), AngleUnits.DEGREES);

				// System.out.println("te,ce " + telemetryError + " " +
				// compassData.getError() + " v "
				// + compassData.getError());
				//
				// double totalError = telemetryError + compassData.getError();
				//
				// // we can be sure that totalError and totalProportioning will
				// // never be zero because MIN_TELEMETRY_ERROR_DEGREES is > 0
				//
				// // as the error in the telemetry gets larger the
				// proportioning
				// // to the compass increases and vice a versa
				// double compassProportioning = telemetryError / totalError;
				// double telemetryProportioning = compassData.getError() /
				// totalError;
				//
				// System.out.println("compP,teleProp " + compassProportioning +
				// " " + telemetryProportioning);
				//
				// // now addjust the proportioning to add up to 100%
				// double totalProportioning = telemetryProportioning +
				// compassProportioning;
				//
				// double tp = telemetryProportioning / totalProportioning;
				// double cp = compassProportioning / totalProportioning;
				//
				// System.out.println("cp,tp " + cp + " " + tp);
				//
				// double change = (tp * telemetryChangeInHeading) + (cp *
				// compassChangeInHeading);
				//
				// System.out.println("change " + change + " " +
				// heading.getDegrees());
				// // System.out.println("Compass: " + angle.getDegrees()
				// // + " heading: " + heading.getDegrees()
				// // + " changeInHeading: " + changeInHeading);
				//
				// // fail safe, compass can't move us more than the
				// telemetry+1.0
				// // deg/second assuming 5 updates per second. So if the
				// compass
				// // is going nuts due to the magnetic field of the fridge our
				// // heading will be still reasonable stable.
				// // change = Math.min(change, telemetryChangeInHeading + 0.2);
				//
				// // calculate the introduced error
				// double addedError = (telemetryError * tp) +
				// (compassData.getError() * cp);
				//
				// // add the introduced error and average it.
				// headingError = (headingError + addedError) / 2.0;
				// System.out.println("addError, newError" + addedError + " " +
				// headingError);
				//
				// heading = heading.add(change, AngleUnits.DEGREES);
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
			return new HeadingData((float) heading.getDegrees(), (float) headingError);
		}
	}

	// public void setHeading(int heading2)
	// {
	// heading = heading2;
	//
	// }

}
