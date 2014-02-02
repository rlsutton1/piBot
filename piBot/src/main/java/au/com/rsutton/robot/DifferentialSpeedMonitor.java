package au.com.rsutton.robot;

import java.util.concurrent.TimeUnit;

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
		pps = ((lastPps * 0.30d) + (pps * 0.70d)) / 2.0d;
		lastPps = pps;
		QuadratureToDistance qts = new QuadratureToDistance();
		Distance dist = qts.scale((int) pps);
		return new Speed(dist, new Time(1, TimeUnit.SECONDS));

	}

	synchronized void pulseOnLeft(int value)
	{
		long now = System.currentTimeMillis();
		long duration = Math.max(MINIMUM_POSSIBLE_DURATION, now - lastLeftPulse);
		float dir = Math.signum(value - lastLeftValue);
		if (dir == 0)
			dir = 1;
		leftPulsesPerSecond = (long) ((1000 / duration) * dir);
		lastLeftPulse = now;
		lastLeftValue = value;
	}

	synchronized void pulseOnRight(int value)
	{
		long now = System.currentTimeMillis();
		long duration = Math.max(MINIMUM_POSSIBLE_DURATION, now - lastRightPulse);
		float dir = Math.signum(value - lastRightValue);
		if (dir == 0)
			dir = 1;
		rightPulsesPerSecond = (long) ((1000 / duration) * dir);
		lastRightPulse = now;
		lastRightValue = value;

	}
}
