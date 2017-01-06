package au.com.rsutton.mapping.particleFilter;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Test;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.hazelcast.SetMotion;
import au.com.rsutton.mapping.KitchenMapBuilder;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.navigation.Navigator;
import au.com.rsutton.navigation.NavigatorControl;
import au.com.rsutton.navigation.router.RouteOption;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.robot.RobotListener;

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

		if (buildMap)
		{
			startPosition = StartPosition.ZERO;
			createInitalMapForMapBuilder();
		}
		final ParticleFilterImpl pf = new ParticleFilterImpl(map, 1000, 1, 1, startPosition);

		NavigatorControl navigator = new Navigator(map, pf, robot);

		if (buildMap)
		{
			buildMap(navigator, pf, map);
		} else
		{
			navigateTo(navigator, 120, -260);
		}

		navigator.stop();

		Thread.sleep(1000);

	}

	private void createInitalMapForMapBuilder()
	{
		map = new ProbabilityMap(5);

		new InitialWorldBuilder(map, robot, 0);

	}

	private void buildMap(NavigatorControl navigator, ParticleFilterIfc pf, ProbabilityMap map)
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
		navigator.calculateRouteTo(x, y, 0, RouteOption.ROUTE_THROUGH_UNEXPLORED);
		navigator.go();

		while (!navigator.hasReachedDestination())//
		{
			Thread.sleep(100);
		}
	}

	class RobotImple implements RobotInterface
	{

		double heading = 0;
		Speed speed = new Speed(new Distance(0, DistanceUnit.CM), Time.perSecond());
		private boolean freeze;

		RobotImple()
		{
			RobotLocation robotDataBus = new RobotLocation();
			robotDataBus.addMessageListener(new MessageListener<RobotLocation>()
			{

				@Override
				public void onMessage(Message<RobotLocation> message)
				{
					for (RobotListener listener : listeners)
					{
						listener.observed(message.getMessageObject());
					}

				}
			});
		}

		@Override
		public void setSpeed(Speed speed)
		{
			this.speed = speed;

		}

		@Override
		public void setHeading(double normalizeHeading)
		{
			this.heading = normalizeHeading;

		}

		@Override
		public void publishUpdate()
		{
			SetMotion motion = new SetMotion();
			motion.setFreeze(freeze);
			motion.setSpeed(speed);
			motion.setHeading(heading);
			motion.publish();

		}

		@Override
		public void freeze(boolean b)
		{
			freeze = b;

		}

		List<RobotListener> listeners = new LinkedList<>();

		@Override
		public void addMessageListener(final RobotListener listener)
		{
			listeners.add(listener);

		}

		@Override
		public void removeMessageListener(RobotListener listener)
		{
			listeners.remove(listener);
		}
	};

	protected RobotInterface getRobot()
	{
		return new RobotImple();
	}
}
