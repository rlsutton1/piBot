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
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.robot.RobotListener;

public class ParticleFilterNavigatorLiveTest
{

	List<Tuple<Double, Double>> headingTuples = new CopyOnWriteArrayList<>();

	@Test
	public void test() throws InterruptedException
	{

		ProbabilityMap map;

		map = KitchenMapBuilder.buildKitchenMap();
		// map = new ProbabilityMap(10);

		RobotInterface robot = getRobot();

		// new InitialWorldBuilder(map, robot, 0);

		final ParticleFilter pf = new ParticleFilter(map, 2000, 1.25, 1.50, StartPosition.RANDOM);

		NavigatorControl navigator = new Navigator(map, pf, robot);

		navigator.calculateRouteTo(120, -260, 0);
		navigator.go();

		while (!navigator.hasReachedDestination())//
		{
			Thread.sleep(100);
		}

		// MapBuilder mapBuilder = new MapBuilder(map, pf, navigator);
		// while (!mapBuilder.isComplete())
		// {
		// Thread.sleep(100);
		// }

		navigator.stop();

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
