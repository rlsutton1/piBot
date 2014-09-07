package au.com.rsutton.mapping;

import au.com.rsutton.cv.RangeFinderConfiguration;

public class CoordResolver
{
	/**
	 * camra is mounted below the laser
	 */

	double camraLaserSeparation = 105; // mm
	double xDegrees = 45;
	double yZeroDegrees = 35;
	double yMaxDegrees = 30;
	double yResolution = 480;
	double xResolution = 640;

	double HALF_X_RESOLUTION = xResolution / 2;
	double HALF_X_ANGLE_RANGE = xDegrees / 2;
	private int orientationToRobot;

	/**
	 * 
	 * @param camraLaserSeparation
	 *            - how many millimetres the laser is above the camera lens
	 *            (centre to centre)
	 * @param xFieldOfViewRangeDegrees
	 *            - the domain of the x axis in degrees
	 * @param yZeroDegrees
	 *            - y coordinate of zero degrees, if you cast a point directly
	 *            in front of the lens an infinite distance away then the y
	 *            value of this point as seen by the camera is this value
	 * @param yMaxDegrees
	 *            - what the angle is for a point when y = 0
	 * @param xRes
	 *            - x resolution of the camra
	 * @param yRes
	 *            - y resolution of the camra
	 */
	public CoordResolver(int camraLaserSeparation,
			int xFieldOfViewRangeDegrees, int yZeroDegrees, int yMaxDegrees,
			int xRes, int yRes)
	{
		this.camraLaserSeparation = camraLaserSeparation;
		xDegrees = xFieldOfViewRangeDegrees;
		this.yZeroDegrees = yZeroDegrees;
		this.yMaxDegrees = yMaxDegrees;
		xResolution = xRes;
		yResolution = yRes;

		HALF_X_RESOLUTION = xResolution / 2;
		HALF_X_ANGLE_RANGE = xDegrees / 2;

	}

	public CoordResolver(RangeFinderConfiguration rangeFinderConfig)
	{
		camraLaserSeparation = rangeFinderConfig.getCamraLaserSeparation();
		xDegrees = rangeFinderConfig.getxFieldOfViewRangeDegrees();
		yZeroDegrees = rangeFinderConfig.getyZeroDegrees();
		yMaxDegrees = rangeFinderConfig.getyMaxDegrees();
		xResolution = rangeFinderConfig.getxRes();
		yResolution = rangeFinderConfig.getyRes();
		orientationToRobot = rangeFinderConfig.getOrientationToRobot();

		HALF_X_RESOLUTION = xResolution / 2;
		HALF_X_ANGLE_RANGE = xDegrees / 2;

	}

	/**
	 * 
	 * @param y
	 * @return -1 if values are invalid
	 */
	private double convertYtoRange(double y)
	{
		double range = -1;
		// sohcahtoa

		// double yAngle = camraAboveLaser(y);

		double yAngle = laserAboveCamra(y);

		if (yAngle > 0.0 || yAngle < 0.0)
		{
			range = Math.tan(Math.toRadians(yAngle)) * camraLaserSeparation;
		}
		range = Math.max(-1, range);
		return range;
	}

	private double camraAboveLaser(double y)
	{
		double yAngle = 90.0
				- ((y - yZeroDegrees) / (yResolution - yZeroDegrees))
				* yMaxDegrees;
		return yAngle;
	}

	private double laserAboveCamra(double y)
	{
		double yAngle = 90.0 - ((yZeroDegrees - y) / (yZeroDegrees))
				* yMaxDegrees;
		return yAngle;
	}

	private double convertXtoAngle(double d)
	{
		double offsetX = d - HALF_X_RESOLUTION;
		//offsetX = HALF_X_ANGLE_RANGE-offsetX;
		return (int) (offsetX * (HALF_X_ANGLE_RANGE / HALF_X_RESOLUTION));
	}

	public XY convertImageXYtoAbsoluteXY(double imageX, double imageY)
	{
		// convert image xy to xy ranges
		double xAngle = convertXtoAngle(imageX);
		double yRange = convertYtoRange(imageY);
		double xRange = Math.sin(Math.toRadians(xAngle)) * yRange;

		
		return Translator2d.rotate(new XY((int) xRange, (int) yRange),
				orientationToRobot);

	}
}
