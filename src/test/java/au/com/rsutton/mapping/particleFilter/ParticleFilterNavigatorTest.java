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

		ProbabilityMap realWorld = KitchenMapBuilder.buildKitchenMap();
		RobotInterface robot = getRobot(realWorld);

		ProbabilityMap map = new ProbabilityMap(10);
		InitialWorldBuilder a = new InitialWorldBuilder(map, robot, 90);

		final ParticleFilterIfc pf = new ParticleFilter(map, 2000, 0.75, 1.0, StartPosition.RANDOM);

		NavigatorControl navigator = new Navigator(map, pf, robot);

		MapBuilder mapBuilder = new MapBuilder(map, pf, navigator);

		// navigator.calculateRouteTo(120, -260, 0);
		// navigator.go();

		while (!mapBuilder.isComplete())
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
