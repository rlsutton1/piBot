package au.com.rsutton.units;

public class HeadingHelper
{

	static public double normalizeHeading(double heading)
	{
		return AngleUtil.normalize(heading);
		// double normalizedHeading = heading;
		// while (normalizedHeading < 0)
		// {
		// normalizedHeading = 360 + normalizedHeading;
		// }
		//
		// return normalizedHeading % 360;
	}

	/**
	 * 
	 * @param newHeading
	 * @param oldHeading
	 * @return change in heading in the range of -180 to +180
	 */
	static public double getChangeInHeading(double newHeading, double oldHeading)
	{
		double newNormalHeading = normalizeHeading(newHeading);
		double oldNormalHeading = normalizeHeading(oldHeading);
		// assume new and heading are both 0..359 normalized (e.g. compass
		// readings)

		if (oldNormalHeading < newNormalHeading)
			oldNormalHeading += 360; // denormalize ...
		double left = oldNormalHeading - newNormalHeading; // calculate left
															// turn, will
		// allways be 0..359

		double ret = left * -1;
		// take the smallest turn
		if (left > 180)
		{
			// Turn right : 360-left degrees
			ret = (360 - left);
		}

		return ret;
	}
}
