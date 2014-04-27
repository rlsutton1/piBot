package com.pi4j.gpio.extension.pixy;

import java.io.Serializable;

import com.pi4j.gpio.extension.pixy.PixyCmu5.Frame;

public class PixyLaserRange implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1872147562491236355L;
	// Y-axis camera/laser data
	private static final double VERTICAL_SEPARATION_BETWEEN_LASER_AND_CAMER = 10.0d;
	private static final double Y_DOMAIN = 200;
	private static final double Y_CENTER = 112;
	private static final double ANGLE_AT_BOTTOM_OF_IMAGE = 60;
	private static final double MAX_Y_ANGLE = 89.95;
	private static final double ACCURACY_LIMIT = 200;// 200 cm

	// X-axis camera data
	private static double MAX_X_ANGLE = 45;
	private static double X_DOMAIN = 255;


	public DistanceVector convertFrameToRangeData(Frame frame)
	{

		if (frame.yCenter > Y_CENTER && frame.height > 0)
		{
			// System.out.println("X " + frame.xCenter + " Y " + frame.yCenter
			// + " w " + frame.width + " h " + frame.height + "s "
			// + frame.signature);

			// calculate y distance
			double y = Y_DOMAIN - (frame.yCenter + (frame.height / 2));
			double yRange = Y_DOMAIN - Y_CENTER;
			double yAngleRange = MAX_Y_ANGLE - ANGLE_AT_BOTTOM_OF_IMAGE;
			double angle = ANGLE_AT_BOTTOM_OF_IMAGE
					+ ((y * yAngleRange) / yRange);

			double yDistance = Math.tan(Math.toRadians(angle))
					* VERTICAL_SEPARATION_BETWEEN_LASER_AND_CAMER;
			// System.out.println("Angle " + angle + " Distance " + yDistance);

			if (yDistance > ACCURACY_LIMIT)
			{
				// outside of our accuracy range
				return null;
			}

			// calculate angle

			double halfDomain = X_DOMAIN / 2d;
			double xPosition = halfDomain - frame.xCenter;
			// reverse direction of angle to clockwise
			xPosition = xPosition * -1d;
			double vectorAngle = xPosition * (MAX_X_ANGLE / halfDomain);

			double vectorDistance = yDistance
					/ Math.cos(Math.toRadians(vectorAngle));
			return new DistanceVector(vectorDistance, vectorAngle);

		}
		//bad data
		return null;
	}
}
