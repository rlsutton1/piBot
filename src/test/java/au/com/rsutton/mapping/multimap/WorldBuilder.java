package au.com.rsutton.mapping.multimap;

import java.util.Random;

import org.junit.Test;

import au.com.rsutton.mapping.KitchenMapBuilder;
import au.com.rsutton.mapping.particleFilter.InitialWorldBuilder;
import au.com.rsutton.mapping.particleFilter.ParticleFilterImpl;
import au.com.rsutton.mapping.particleFilter.StartPosition;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.navigation.Navigator;
import au.com.rsutton.navigation.NavigatorControl;
import au.com.rsutton.navigation.router.RouteOption;
import au.com.rsutton.robot.RobotSimulator;

public class WorldBuilder
{

	@Test
	public void build() throws InterruptedException
	{

		RobotSimulator robot = new RobotSimulator(KitchenMapBuilder.buildKitchenMap());
		robot.setLocation(-150, 300, new Random().nextInt(360));
		ProbabilityMapProxy map = new ProbabilityMapProxy(new ProbabilityMap(5));
		new InitialWorldBuilder(map, robot);

		ParticleFilterProxy pf = new ParticleFilterProxy(
				new ParticleFilterImpl(map, 1000, 0.3, 0.3, StartPosition.ZERO));

		NavigatorControl navigator = new Navigator(map, pf, robot);

		for (int y = -50; y > -200; y -= 50)
		{
			navigator.calculateRouteTo(0, y, 0, RouteOption.ROUTE_THROUGH_CLEAR_SPACE_ONLY);
			navigator.go();
			while (!navigator.hasReachedDestination())
			{
				Thread.sleep(1000);
			}
			ProbabilityMap nextMap = new ProbabilityMap(5);
			new SegmentMapBuilder(nextMap, navigator, robot, 0, y, 0, pf);

			map.changeMap(nextMap);

		}

	}
}
