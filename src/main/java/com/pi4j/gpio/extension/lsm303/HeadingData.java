package com.pi4j.gpio.extension.lsm303;

import java.io.Serializable;

public class HeadingData implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private float heading;
	private float error;
	private long stamp = System.currentTimeMillis();

	public HeadingData()
	{

	}

	public HeadingData(float heading, float error)
	{
		this.heading = heading;
		this.error = error;
	}

	/**
	 * heading in degress
	 * 
	 * @return
	 */
	public float getHeading()
	{
		return heading;
	}

	/**
	 * error in degress +- (always positive)
	 * 
	 * @return
	 */
	public float getError()
	{
		// error increases by 1 degree every 100ms
		long errorIncreaseWithElapsedTime = (System.currentTimeMillis() - stamp) / 100;

		// minimum error 0.1
		return (float) (0.1 + error) + errorIncreaseWithElapsedTime;
	}

}