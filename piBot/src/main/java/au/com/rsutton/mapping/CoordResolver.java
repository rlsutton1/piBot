package au.com.rsutton.mapping;

public class CoordResolver
{
	/**
	 * camra is mounted above the laser
	 */

	double camraLaserSeparation = 105; // mm
	double xDegrees = 45;
	double yZeroDegrees = 35;
	double yMaxDegrees = 30;
	double yResolution = 480;
	double xResolution = 640;

	double HALF_X_RESOLUTION = xResolution / 2;
	double HALF_X_ANGLE_RANGE = xDegrees / 2;

	public double convertYtoRange(double y)
	{
		double range = -1;
		// sohcahtoa
		if (y > yZeroDegrees)
		{
			double yAngle = 90.0
					- ((y - yZeroDegrees) / (yResolution - yZeroDegrees))
					* yMaxDegrees;
			range = Math.tan(Math.toRadians(yAngle)) * camraLaserSeparation;
		}
		return range;
	}

	public double convertXtoAngle(double d)
	{
		return (int) ((d - HALF_X_RESOLUTION) * (HALF_X_ANGLE_RANGE / HALF_X_RESOLUTION));
	}
}
