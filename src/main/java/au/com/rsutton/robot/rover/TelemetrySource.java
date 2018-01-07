package au.com.rsutton.robot.rover;

import com.pi4j.gpio.extension.lsm303.HeadingData;

import au.com.rsutton.units.Distance;

public interface TelemetrySource
{

	public Distance getTotalDistanceTravelled();

	public HeadingData getHeading();
}
