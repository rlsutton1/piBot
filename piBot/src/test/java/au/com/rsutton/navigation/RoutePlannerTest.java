package au.com.rsutton.navigation;

import static org.junit.Assert.*;

import org.junit.Test;

import au.com.rsutton.mapping.particleFilter.KitchenMapBuilder;

public class RoutePlannerTest
{

	@Test
	public void test()
	{
		RoutePlanner planner = new RoutePlanner(KitchenMapBuilder.buildKitchenMap());
		
		planner.createRoute(30, -140);
		planner.dumpRoute();
		
		System.out.println(planner.getRouteForLocation(-40, 70));
	}

}
