package au.com.rsutton.mapping.v3.linearEquasion;

import au.com.rsutton.mapping.XY;
import au.com.rsutton.robot.rover.Angle;

public interface LinearEquasion
{

	InterceptResult getIntercept(LinearEquasion otherLine);

	public Angle getAngle();

	/**
	 * 
	 * @param otherLine
	 * @param angleTolleranceDegrees
	 *            - number of degrees the lines may be different by and still
	 *            considered a match
	 * @param cTollerance
	 *            - proximity tollerance
	 * @param at
	 *            - location to check for the lines being at close proximity
	 * @return
	 */
	boolean isSimilar(LinearEquasion otherLine, double angleTolleranceDegrees,
			double cTollerance, XY at);
}
