package au.com.rsutton.navigation.router;

import au.com.rsutton.navigation.NextMove;
import au.com.rsutton.units.Distance;

public interface RoutePlanner
{

	/**
	 * 
	 * @param toX
	 * @param toY
	 * @param routeOption
	 * @return true if a route was successfully built
	 */
	boolean createRoute(int toX, int toY, RouteOption routeOption);

	ExpansionPoint getRouteForLocation(int x, int y);

	boolean hasPlannedRoute();

	Distance getDistanceToTarget(int pfX, int pfY);

	double getTurnRadius();

	int getDirection();

	NextMove getNextMove(int pfX, int pfY, double heading);

}