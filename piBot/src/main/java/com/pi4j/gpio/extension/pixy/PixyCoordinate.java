package com.pi4j.gpio.extension.pixy;

import java.io.Serializable;

public class PixyCoordinate implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2471395283516435063L;
	double x;
	double y;
	double count;

	public double getAverageX()
	{
		return x / count;
	}

	public double getAverageY()
	{
		return y / count;
	}
}
