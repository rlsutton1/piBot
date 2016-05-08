package com.pi4j.gpio.extension.lsm303;

public class CompassAxisCalibractionCorrection
{
	private int min;
	private int range;

	CompassAxisCalibractionCorrection(int min, int max)
	{
		this.min = Math.min(min, max);
		range = Math.abs(min - max);
	}

	public int getCorrectedValue(int value)
	{
		return (value - min) - (range / 2);

	}
}
