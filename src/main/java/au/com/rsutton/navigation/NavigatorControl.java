package au.com.rsutton.navigation;

import au.com.rsutton.navigation.router.ExpansionPoint;
import au.com.rsutton.navigation.router.RouteOption;

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
	void calculateRouteTo(int x, int y, Double heading, RouteOption routeOption);

	boolean hasReachedDestination();

	boolean isStuck();

	boolean isStopped();

	ExpansionPoint getRouteForLocation(int x, int y);

}
