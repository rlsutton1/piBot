package au.com.rsutton.navigation.router;

import org.junit.Test;

import au.com.rsutton.mapping.KitchenMapBuilder;
import au.com.rsutton.navigation.router.RouteOption;
import au.com.rsutton.navigation.router.RoutePlanner;

public class RoutePlannerTest
{

	@Test
	public void test()
	{
		RoutePlanner planner = new RoutePlanner(KitchenMapBuilder.buildKitchenMap());

		planner.createRoute(30, -140, RouteOption.ROUTE_THROUGH_UNEXPLORED);
		planner.dumpRoute();

		System.out.println(planner.getRouteForLocation(-40, 70));
	}

}
