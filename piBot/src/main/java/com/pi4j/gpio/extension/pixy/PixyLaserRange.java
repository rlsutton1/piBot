package com.pi4j.gpio.extension.pixy;

import java.io.Serializable;

public class PixyLaserRange implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1872147562491236355L;
	// Y-axis camera/laser data
	private static final double VERTICAL_SEPARATION_BETWEEN_LASER_AND_CAMERA = 10.0d;
	private static final double Y_DOMAIN = 200;
	static final double Y_CENTER = 112;
	private static final double ANGLE_AT_BOTTOM_OF_IMAGE = 60;
	private static final double MAX_Y_ANGLE = 89.95;
	private static final double ACCURACY_LIMIT = 200;// 200 cm

	// X-axis camera data
	private static double MAX_X_ANGLE = 45;
	private static double X_DOMAIN = 300;

	// Map<Integer,DataValueSmoother> stabilizer = new HashMap<>();

	public DistanceVector convertFrameToRangeData(Frame frame,
			int referenceDistance)
	{

		if (frame.yCenter > Y_CENTER && frame.height > 0)
		{

			// System.out.println("X " + frame.xCenter + " Y " + frame.yCenter
			// + " w " + frame.width + " h " + frame.height + "s "
			// + frame.signature);

			// calculate y distance
			double y = Y_DOMAIN - (frame.yCenter);
			double yRange = Y_DOMAIN - Y_CENTER;
			double yAngleRange = MAX_Y_ANGLE - ANGLE_AT_BOTTOM_OF_IMAGE;
			double angle = ANGLE_AT_BOTTOM_OF_IMAGE
					+ ((y * yAngleRange) / yRange);

			// calculate xangle

			double halfDomain = X_DOMAIN / 2d;
			double xPosition = halfDomain - frame.xCenter;
			// reverse direction of angle to clockwise
			xPosition = xPosition * -1d;
			double vectorAngle = xPosition * (MAX_X_ANGLE / halfDomain);

			// 1.25/tan((86-x)/180)

			// outer = 1.5/tan((86-(x))/180)
			//
			// center= 2/tan((86-(x))/180)

			double xadjust = vectorAngle / (0.5d / MAX_X_ANGLE);
			xadjust = 2d - xadjust;

			double yDistance = xadjust * Math.tan(Math.toRadians(86d - angle));

			// previous near working formula

			// double yDistance = Math.tan(Math.toRadians(angle))
			// * VERTICAL_SEPARATION_BETWEEN_LASER_AND_CAMERA;
			//
			
			// System.out.println("Angle " + angle + " Distance " + yDistance);

			if (yDistance > ACCURACY_LIMIT)
			{
				// outside of our accuracy range
				return null;
			}

			System.out.println("fx " + frame.xCenter + " va " + vectorAngle
					+ " yd " + yDistance);
			double vectorDistance = yDistance
					/ Math.cos(Math.toRadians(vectorAngle));
			System.out.println("yc " + frame.yCenter + " fx " + frame.xCenter
					+ " va " + vectorAngle + " yd " + yDistance + " vd "
					+ vectorDistance);

			return new DistanceVector(vectorDistance, vectorAngle, angle,
					yDistance, referenceDistance);

		}
		// bad data
		return null;
	}
}
