package au.com.rsutton.navigation;

import au.com.rsutton.units.Pose;

public interface NavigatorControl
{

	/**
	 * stop immediately
	 */
	void stop();

	/**
	 * start or resume the journey
	 */
	void go();

	/**
	 * calculate a new route to the provided destination using the current map
	 * state
	 * 
	 * @param x
	 * @param y
	 * @param heading
	 */
	void calculateRouteTo(Pose pose);

	boolean hasReachedDestination();

	boolean isStuck();

}
