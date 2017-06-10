package au.com.rsutton.mapping.particleFilter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Test;

import au.com.rsutton.mapping.KitchenMapBuilder;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.navigation.Navigator;
import au.com.rsutton.navigation.NavigatorControl;
import au.com.rsutton.navigation.router.RouteOption;
import au.com.rsutton.robot.RobotInterface;

public class ParticleFilterNavigatorLiveTest
{

	List<Tuple<Double, Double>> headingTuples = new CopyOnWriteArrayList<>();
	ProbabilityMap map = KitchenMapBuilder.buildKitchenMap();
	RobotInterface robot = getRobot();

	@Test
	public void test() throws InterruptedException
	{
		boolean buildMap = true;
		StartPosition startPosition = StartPosition.RANDOM;
		Navigator navigator;
		if (buildMap)
		{
			startPosition = StartPosition.ZERO;
			createInitalMapForMapBuilder();
			// pf = new ParticleFilterImpl(map, 1000, 5.8, 5.8, startPosition,
			// robot, null);

			// use slam instead of a particle filter
			navigator = new Navigator(map, null, robot);
		} else
		{
			final ParticleFilterImpl pf = new ParticleFilterImpl(map, 1000, 1, 1, startPosition, robot, null);
			navigator = new Navigator(map, pf, robot);
		}

		if (buildMap)
		{
			buildMap(navigator, navigator.getSlam(), map);
		} else
		{
			navigateTo(navigator, 120, -260);
			navigateTo(navigator, -130, 300);
		}

		navigator.stop();

		Thread.sleep(1000);

	}

	protected void createInitalMapForMapBuilder()
	{
		map = new ProbabilityMap(5);

		// new InitialWorldBuilder(map, robot);

	}

	private void buildMap(NavigatorControl navigator, RobotPoseSource pf, ProbabilityMapIIFc map)
			throws InterruptedException
	{
		MapBuilder mapBuilder = new MapBuilder(map, pf, navigator, robot);
		while (!mapBuilder.isComplete())
		{
			Thread.sleep(100);
		}

	}

	private void navigateTo(NavigatorControl navigator, int x, int y) throws InterruptedException
	{
		navigator.calculateRouteTo(x, y, 0d, RouteOption.ROUTE_THROUGH_UNEXPLORED);
		navigator.go();

		while (!navigator.hasReachedDestination())//
		{
			Thread.sleep(100);
		}
	}

	protected RobotInterface getRobot()
	{
		return new RobotImple();
	}
}
