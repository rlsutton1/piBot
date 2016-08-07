package au.com.rsutton.mapping.particleFilter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Test;

import au.com.rsutton.mapping.probability.ProbabilityMap;

public class ParticleFilterNavigatorTest
{

	List<Tuple<Double, Double>> headingTuples = new CopyOnWriteArrayList<>();

	@Test
	public void test() throws InterruptedException
	{

		ProbabilityMap map = KitchenMapBuilder.buildKitchenMap();

		final ParticleFilter pf = new ParticleFilter(map, 2000, 0.75, 1.0);

		RobotInterface robot = getRobot(map);

		NavigatorControl navigator = new Navigator(map, pf, robot);

		navigator.calculateRouteTo(120, -260, 0);
		navigator.go();

		while (!navigator.hasReachedDestination())
		{
			Thread.sleep(100);
		}

		navigator.stop();

	}

	private RobotSimulator getRobot(ProbabilityMap map)
	{
		RobotSimulator robot = new RobotSimulator(map);
		robot.setLocation(-150, 300, 0);
		return robot;
	}
}
