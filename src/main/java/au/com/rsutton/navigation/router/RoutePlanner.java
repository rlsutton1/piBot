package au.com.rsutton.navigation.router;

import au.com.rsutton.ui.DataSourceMap;

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

	double getDistanceToTarget(int pfX, int pfY);

	DataSourceMap getGdPointSource();

	double getTurnRadius();

	int getDirection();

}