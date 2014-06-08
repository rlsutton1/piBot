package com.pi4j.gpio.extension.pixy;

import java.io.Serializable;

public class DistanceVector implements Serializable
{
	private static final long serialVersionUID = -575716070160734588L;

	// test variables
	double yAngle;
	double ydistance;
	int referenceDistance;

	public DistanceVector(double vectorDistance, double vectorAngle,
			double yAngle, double yDistance, int referenceDistance)
	{
		distance = vectorDistance;
		angle = vectorAngle;
		this.yAngle = yAngle;
		this.ydistance = yDistance;
		this.referenceDistance = referenceDistance;
	}

	@Override
	public String toString()
	{
		return distance + "," + angle + "," + yAngle + "," + ydistance + ","
				+ referenceDistance;
	}

	public double distance;
	public final double angle;

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		long temp;
		temp = (long) angle;
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + referenceDistance;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DistanceVector other = (DistanceVector) obj;
		if (((int) angle) != ((int) other.angle))
			return false;
		if (((int) referenceDistance) != ((int) other.referenceDistance))
			return false;
		return true;
	}

}
