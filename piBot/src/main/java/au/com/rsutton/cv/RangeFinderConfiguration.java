package au.com.rsutton.cv;

import java.io.Serializable;

import au.com.rsutton.cv.dsl.DSLbuild;
import au.com.rsutton.cv.dsl.DSLcamerLaserSeparation;
import au.com.rsutton.cv.dsl.DSLsetOrientationToRobot;
import au.com.rsutton.cv.dsl.DSLxFeildOfView;
import au.com.rsutton.cv.dsl.DSLyMax;
import au.com.rsutton.cv.dsl.DSLyZeroDegrees;

public class RangeFinderConfiguration implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7382049593284539884L;
	int camraLaserSeparation;
	int xFieldOfViewRangeDegrees;
	int yZeroDegrees;
	int yMaxDegrees;
	int xRes;
	int yRes;
	int orientationToRobot;

	static final class Builder implements DSLyMax, DSLcamerLaserSeparation,
			DSLxFeildOfView, DSLyZeroDegrees, DSLbuild,
			DSLsetOrientationToRobot
	{
		int camraLaserSeparation;
		int xFieldOfViewRangeDegrees;
		int yZeroDegrees;
		int yMaxDegrees;
		int xRes;
		int yRes;
		int orientationToRobot;

		public RangeFinderConfiguration build()
		{
			RangeFinderConfiguration config = new RangeFinderConfiguration();

			if (xRes == 0 || yRes == 0)
			{
				throw new RuntimeException("call setCameraResolution first");
			}

			config.camraLaserSeparation = camraLaserSeparation;
			config.xFieldOfViewRangeDegrees = xFieldOfViewRangeDegrees;
			config.yZeroDegrees = yZeroDegrees;
			config.yMaxDegrees = yMaxDegrees;
			config.xRes = xRes;
			config.yRes = yRes;
			config.orientationToRobot = orientationToRobot;
			return config;
		}

		public DSLsetOrientationToRobot setCameraLaserSeparation(int mm)
		{
			camraLaserSeparation = mm;
			return this;
		}

		public DSLbuild setOrientationToRobot(int angle)
		{
			orientationToRobot = angle;
			return this;
		}

		public DSLcamerLaserSeparation setXFieldOfViewRangeDegrees(int d)
		{
			xFieldOfViewRangeDegrees = d;
			return this;
		}

		public DSLxFeildOfView setYZeroDegrees(int d)
		{
			yZeroDegrees = d;
			return this;
		}

		public DSLyZeroDegrees setYMaxDegrees(int y)
		{
			yMaxDegrees = y;
			return this;
		}

		DSLyMax setCameraResolution(int x, int y)
		{
			xRes = x;
			yRes = y;
			return this;
		}

	}

	public int getCamraLaserSeparation()
	{
		return camraLaserSeparation;
	}

	public int getxFieldOfViewRangeDegrees()
	{
		return xFieldOfViewRangeDegrees;
	}

	public int getyZeroDegrees()
	{
		return yZeroDegrees;
	}

	public int getyMaxDegrees()
	{
		return yMaxDegrees;
	}

	public int getxRes()
	{
		return xRes;
	}

	public int getyRes()
	{
		return yRes;
	}

	public int getOrientationToRobot()
	{
		return orientationToRobot;
	}

}
