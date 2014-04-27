package com.pi4j.gpio.extension.pixy;

import java.io.Serializable;

public class DistanceVector implements Serializable
{
	private static final long serialVersionUID = -575716070160734588L;

	public DistanceVector(double vectorDistance, double vectorAngle)
	{
		distance = vectorDistance;
		angle = vectorAngle;
	}

	@Override
	public String toString()
	{
		return "DistanceVector [distance=" + distance + ", angle=" + angle
				+ "]";
	}

	public final double distance;
	public final double angle;
}
