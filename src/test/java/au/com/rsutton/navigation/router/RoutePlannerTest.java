package au.com.rsutton.navigation.router;

import org.junit.Test;

import au.com.rsutton.mapping.KitchenMapBuilder;

public class RoutePlannerTest
{

	@Test
	public void test()
	{
		RoutePlannerImpl planner = new RoutePlannerImpl(KitchenMapBuilder.buildKitchenMap());

		planner.createRoute(30, -140, RouteOption.ROUTE_THROUGH_UNEXPLORED);
		planner.dumpRoute();

		ExpansionPoint lastTarget = null;
		ExpansionPoint target = new ExpansionPoint(-40, 70, 0);
		int ctr = 0;
		do
		{
			target = planner.getRouteForLocation(target.getX(), target.getY());
			if (target != null)
			{
				System.out.println(target);
			}
			if (target.equals(lastTarget))
			{
				break;
			}
			lastTarget = target;
			ctr++;
		} while (target != null && ctr < 100);

	}

}
