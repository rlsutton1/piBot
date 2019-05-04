package au.com.rsutton.mapping.particleFilter;

import org.junit.Test;

import au.com.rsutton.kalman.RobotPoseSourceNoop;
import au.com.rsutton.mapping.KitchenMapBuilder;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.navigation.Navigator;
import au.com.rsutton.navigation.NavigatorControl;
import au.com.rsutton.navigation.router.RouteOption;
import au.com.rsutton.robot.RobotInterface;

public class ParticleFilterNavigatorLiveTest
{

	ProbabilityMap map = KitchenMapBuilder.buildMap();
	RobotInterface robot = getRobot();

	@Test
	public void test() throws InterruptedException
	{

		new DataWindow();

		StartPosition startPosition = StartPosition.RANDOM;
		Navigator navigator;
		final RobotPoseSource pf = new RobotPoseSourceNoop(
				new ParticleFilterImpl(map, 5000, 2, 1, startPosition, robot, null));
		navigator = new Navigator(map, pf, robot, 20);

		new SubMapBuilder().buildMap(robot);
		navigateTo(navigator, 120, -260);
		new SubMapBuilder().buildMap(robot);

		navigateTo(navigator, -130, 300);

		navigateTo(navigator, 120, -260);
		navigateTo(navigator, -130, 300);

		navigateTo(navigator, 120, -260);
		navigateTo(navigator, -130, 300);

		navigateTo(navigator, 120, -260);
		navigateTo(navigator, -130, 300);

		navigator.stop();

		Thread.sleep(1000);

	}

	protected void createInitalMapForMapBuilder()
	{
		map = new ProbabilityMap(5);

		// new InitialWorldBuilder(map, robot);

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
