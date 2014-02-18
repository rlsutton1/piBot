package au.com.rsutton.robot;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;

public class DifferentialSpeedMonitor
{
	// because System.currentTimeMillis is not very accurate
	private static final int MINIMUM_POSSIBLE_DURATION = 20;
	long lastLeftPulse = System.currentTimeMillis();
	long lastLeftValue = 0;
	private long leftPulsesPerSecond;

	long lastRightPulse = System.currentTimeMillis();
	long lastRightValue = 0;
	private long rightPulsesPerSecond;
	private double lastPps;

	synchronized Speed getSpeed()
	{
		long now = System.currentTimeMillis();
		if (lastRightPulse < now - 1000)
		{
			rightPulsesPerSecond = 0;
		}
		if (lastLeftPulse < now - 1000)
		{
			leftPulsesPerSecond = 0;
		}

		double pps = leftPulsesPerSecond + rightPulsesPerSecond;
		pps = ((lastPps * 0.70d) + (pps * 0.30d)) ;
		lastPps = pps;
		QuadratureToDistance qts = new QuadratureToDistance();
		Distance dist = qts.scale((int) pps);
		return new Speed(dist,Time.perSecond());

	}

	synchronized void pulseOnLeft(int value)
	{
		long now = System.currentTimeMillis();
		long duration = now - lastLeftPulse;
		if (duration > 50)
		{
			float distance = value - lastLeftValue;
			leftPulsesPerSecond = (long) ((1000 / duration) * distance);
			lastLeftPulse = now;
			lastLeftValue = value;
		}
	}

	synchronized void pulseOnRight(int value)
	{
		long now = System.currentTimeMillis();
		long duration = now - lastRightPulse;
		if (duration > 50)
		{
			float distance = value - lastRightValue;
			rightPulsesPerSecond = (long) ((1000 / duration) * distance);
			lastRightPulse = now;
			lastRightValue = value;
		}
	}
}
