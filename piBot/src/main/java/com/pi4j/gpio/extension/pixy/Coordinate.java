package com.pi4j.gpio.extension.pixy;

import java.io.Serializable;

public class Coordinate implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2471395283516435063L;
	double x;
	double y;
	double count;

	public Coordinate(double x, double y)
	{
		this.x = x;
		this.y = y;
		count = 1;
	}

	public double getAverageX()
	{
		return x / count;
	}

	public double getAverageY()
	{
		return y / count;
	}
}
