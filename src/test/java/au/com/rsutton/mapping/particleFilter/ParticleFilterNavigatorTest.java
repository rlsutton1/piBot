package au.com.rsutton.mapping.particleFilter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;

import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.ui.DataSourcePoint;

public class ParticleFilterNavigatorTest
{

	List<Tuple<Double, Double>> headingTuples = new CopyOnWriteArrayList<>();
	private boolean initializing;

	@Test
	public void test() throws InterruptedException
	{

		ProbabilityMap map = KitchenMapBuilder.buildKitchenMap();
		RobotInterface robot = getRobot(map);

		final ParticleFilterIfc pf = new ParticleFilter(map, 2000, 0.75, 1.0, StartPosition.RANDOM);

		initializing = true;

		NavigatorControl navigator = new Navigator(map, pf, robot);

		MapBuilder mapBuilder = new MapBuilder(pf, navigator);

		// navigator.calculateRouteTo(120, -260, 0);
		// navigator.go();

		while (!mapBuilder.isComplete())
		{
			Thread.sleep(100);
		}

		navigator.stop();

	}

	double sudoheading = 0;

	ParticleFilterIfc getParticleFilterWrapper(final ParticleFilter pf)
	{

		double x = 0;
		double y = 0;
		return new ParticleFilterIfc()
		{

			@Override
			public double getAverageHeading()
			{
				if (initializing)
				{
					return sudoheading;
				}
				return pf.getAverageHeading();
			}

			@Override
			public double getStdDev()
			{
				if (pf.getStdDev() < 20 && pf.getBestRating() > 0.4)
				{
					initializing = false;
				}
				if (initializing)
				{
					return 0;
				}
				return pf.getStdDev();
			}

			@Override
			public void addListener(final ParticleFilterListener listener)
			{
				pf.addListener(new ParticleFilterListener()
				{

					@Override
					public void update(Vector3D averagePosition, double averageHeading, double stdDev,
							ParticleFilterObservationSet particleFilterObservationSet)
					{
						if (initializing)
						{
							listener.update(dumpAveragePosition(), getAverageHeading(), 1, particleFilterObservationSet);

						} else
						{

							listener.update(averagePosition, averageHeading, stdDev, particleFilterObservationSet);
						}

					}
				});

			}

			@Override
			public DataSourcePoint getParticlePointSource()
			{
				return pf.getParticlePointSource();
			}

			@Override
			public DataSourceMap getHeadingMapDataSource()
			{
				return pf.getHeadingMapDataSource();
			}

			@Override
			public Double getBestRating()
			{
				if (initializing)
				{
					return 1.0;
				}
				return pf.getBestRating();
			}

			@Override
			public Vector3D dumpAveragePosition()
			{
				if (initializing)
				{
					return new Vector3D(x, y, 0);
				}
				return pf.dumpAveragePosition();
			}

			@Override
			public void moveParticles(ParticleUpdate particleUpdate)
			{
				if (initializing)
				{
					sudoheading += particleUpdate.getDeltaHeading();
				}
				pf.moveParticles(particleUpdate);

			}

			@Override
			public void addObservation(ProbabilityMap currentWorld, ParticleFilterObservationSet observations,
					double compassAdjustment)
			{
				pf.addObservation(currentWorld, observations, compassAdjustment);

			}

			@Override
			public void setParticleCount(int max)
			{
				pf.setParticleCount(max);

			}
		};
	}

	private NavigatorControl getFakeNavigator()
	{
		return new NavigatorControl()
		{

			@Override
			public void stop()
			{
				// TODO Auto-generated method stub

			}

			@Override
			public boolean hasReachedDestination()
			{
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void go()
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void calculateRouteTo(int x, int y, double heading)
			{
				// TODO Auto-generated method stub

			}
		};
	}

	private RobotSimulator getRobot(ProbabilityMap map)
	{
		RobotSimulator robot = new RobotSimulator(map);
		robot.setLocation(-150, 300, 0);
		return robot;
	}
}
