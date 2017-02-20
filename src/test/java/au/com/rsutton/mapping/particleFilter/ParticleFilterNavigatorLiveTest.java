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
		boolean buildMap = false;
		StartPosition startPosition = StartPosition.RANDOM;
		final ParticleFilterImpl pf;
		if (buildMap)
		{
			startPosition = StartPosition.ZERO;
			createInitalMapForMapBuilder();
			pf = new ParticleFilterImpl(map, 1000, 0.3, 0.3, startPosition, robot, null);
		} else
		{
			pf = new ParticleFilterImpl(map, 1000, 1, 1, startPosition, robot, null);
		}

		NavigatorControl navigator = new Navigator(map, pf, robot);

		if (buildMap)
		{
			buildMap(navigator, pf, map);
		} else
		{
			navigateTo(navigator, 120, -260);
		}

		double headingDrift = navigator.getHeadingDrift();

		navigator.stop();

		Thread.sleep(1000);
		System.out.println("Heading drift " + headingDrift);

	}

	protected void createInitalMapForMapBuilder()
	{
		map = new ProbabilityMap(5);

		new InitialWorldBuilder(map, robot);

	}

	private void buildMap(NavigatorControl navigator, ParticleFilterIfc pf, ProbabilityMapIIFc map)
			throws InterruptedException
	{
		MapBuilder mapBuilder = new MapBuilder(map, pf, navigator);
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
