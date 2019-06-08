package au.com.rsutton.mapping.particleFilter;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import com.google.common.base.Stopwatch;

import au.com.rsutton.hazelcast.LidarScan;
import au.com.rsutton.kalman.RobotPoseSourceNoop;
import au.com.rsutton.mapping.BoxMapBuilder;
import au.com.rsutton.mapping.KitchenMapBuilder;
import au.com.rsutton.mapping.XY;
import au.com.rsutton.mapping.probability.Occupancy;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.navigation.Navigator;
import au.com.rsutton.navigation.NavigatorControl;
import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.navigation.feature.RobotLocationDeltaListener;
import au.com.rsutton.navigation.router.RouteOption;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.robot.RobotSimulator;
import au.com.rsutton.ui.CoordinateClickListener;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.ui.DataSourcePoint;
import au.com.rsutton.ui.MapDrawingWindow;
import au.com.rsutton.ui.VideoWindow;
import au.com.rsutton.ui.WrapperForObservedMapInMapUI;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;
import au.com.rsutton.units.Speed;
import au.com.rsutton.units.Time;

public class MapBuilder
{

	private static final int RANGE_LIMIT_FOR_ADD = 150;

	private static final int MIN_TARGET_SEPARATION = (int) (RANGE_LIMIT_FOR_ADD * 0.4);

	private static final double DISTANCE_NOISE = 3;

	private static final double HEADING_NOISE = 3;

	private static final int CHANGE_COUNTER_ADD_MAP = 0;
	private static final int CHANGE_COUNTER_RESET = 10;

	Logger logger = LogManager.getLogger();

	private MapDrawingWindow panel;

	ProbabilityMapIIFc world = new ProbabilityMap(5);

	private NavigatorControl navigatorControl;

	private RobotPoseSource poseAdjuster;
	RobotInterface robot;

	Set<XY> vistedLocations = new HashSet<>();

	boolean addMap = true;

	class SubMapHolder
	{
		public SubMapHolder(Pose pose, ProbabilityMapIIFc map)
		{
			this.setMapPose(pose);
			this.map = map;
		}

		Pose getMapPose()
		{

			return mapPose;
		}

		void setMapPose(Pose mapPose)
		{
			this.mapPose = mapPose;
		}

		private Pose mapPose;
		ProbabilityMapIIFc map;

	}

	List<SubMapHolder> subMaps = new LinkedList<>();

	Stopwatch targetAge = Stopwatch.createStarted();

	private ParticleFilterIfc particleFilterProxy;

	Pose nextTarget = null;
	volatile boolean crashDetected = false;

	final boolean simulator = true;
	int maxSpeed = 15;

	public void test() throws InterruptedException
	{
		try
		{

			Configurator.setRootLevel(Level.ERROR);
			new DataWindow();

			if (simulator)
			{

				maxSpeed = 50;
				boolean useKitchenMap = true;
				RobotSimulator robotS;
				if (useKitchenMap)
				{
					robotS = new RobotSimulator(KitchenMapBuilder.buildMap());

					robotS.setLocation(-150, 100, new Random().nextInt(360));
				} else
				{
					// robotS = new RobotSimulator(LoopMapBuilder.buildMap());

					robotS = new RobotSimulator(BoxMapBuilder.buildMap());

					robotS.setLocation(400, 400, new Random().nextInt(360));
				}
				this.robot = robotS;
			} else
			{
				new VideoWindow("Video Feed", 600, 600);

				this.robot = new RobotImple();
			}

			robot.addMessageListener(new RobotLocationDeltaListener()
			{

				@Override
				public void onMessage(Angle deltaHeading, Distance deltaDistance, boolean bump)
				{
					if (bump)
					{
						crashDetected = true;
						System.out
								.println("bump *********************************************************************");
					}
				}

				@Override
				public void onMessage(LidarScan robotLocation)
				{
					// not used here

				}
			});

			vistedLocations.add(new XY(0, 0));

			panel = new MapDrawingWindow("Map Builder (Clickable)", 0, 0, 1000, true);

			panel.setCoordinateClickListener(new CoordinateClickListener()
			{

				@Override
				public void clickAt(int x, int y)
				{
					logger.error("Navigate to " + x + " " + y);

					nextTarget = new Pose(x, y, 0.0);

					logger.error("Navigate to XXX " + x + " " + y);
				}
			});

			panel.addDataSource(new WrapperForObservedMapInMapUI(world));

			addMap(getZeroPose());

			particleFilterProxy = new ParticleFilterImpl(world, 1000, DISTANCE_NOISE, HEADING_NOISE, StartPosition.ZERO,
					robot, new Pose(0, 0, 0));
			this.poseAdjuster = new RobotPoseSourceNoop(particleFilterProxy);

			this.navigatorControl = new Navigator(world, poseAdjuster, getShimmedRobot(robot), maxSpeed);

			// Thread.sleep(20000);

			chooseTarget();

			int changeCounter = 10;
			boolean localized = false;
			while (!complete)
			{
				TimeUnit.MILLISECONDS.sleep(500);
				if (crashDetected)
				{

					navigatorSuspended = true;

					for (int i = 0; i < 40; i++)
					{
						robot.setSpeed(new Speed(new Distance(-10, DistanceUnit.CM), Time.perSecond()));
						robot.setStraight("crash -backup");
						robot.freeze(false);
						robot.publishUpdate();
						TimeUnit.MILLISECONDS.sleep(100);
					}
					robot.freeze(true);
					robot.publishUpdate();
					crashDetected = false;
					chooseTarget();
					navigatorSuspended = false;
				}

				update();

				if (poseAdjuster != null && poseAdjuster.getParticleFilterStatus() == ParticleFilterStatus.LOCALIZED)
				{
					localized = true;
				}

				if (poseAdjuster != null && poseAdjuster.getParticleFilterStatus() == ParticleFilterStatus.POOR_MATCH
						&& changeCounter < CHANGE_COUNTER_ADD_MAP && localized == true)
				{
					navigatorSuspended = true;
					for (int i = 0; i < 15; i++)
					{
						robot.freeze(true);
						TimeUnit.MILLISECONDS.sleep(100);
					}
					if (addMap)
					{
						addMap(poseAdjuster);
					}

					navigatorSuspended = false;
					changeCounter = CHANGE_COUNTER_RESET;

				}

				changeCounter--;

			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private volatile boolean navigatorSuspended = false;

	private RobotInterface getShimmedRobot(final RobotInterface robot2)
	{
		return new RobotInterface()
		{

			@Override
			public void freeze(boolean b)
			{
				if (!navigatorSuspended)
				{
					robot2.freeze(b);
				} else
				{
					logger.warn("Suspended");
				}

			}

			@Override
			public void setSpeed(Speed speed)
			{
				if (!navigatorSuspended)
				{
					robot2.setSpeed(speed);
				} else
				{
					logger.warn("Suspended");
				}
			}

			@Override
			public void setTurnRadius(double turnRadius)
			{
				if (!navigatorSuspended)
				{
					robot2.setTurnRadius(turnRadius);
				} else
				{
					logger.warn("Suspended");
				}
			}

			@Override
			public void publishUpdate()
			{
				if (!navigatorSuspended)
				{
					robot2.publishUpdate();
				} else
				{
					logger.warn("Suspended");
				}
			}

			@Override
			public void addMessageListener(RobotLocationDeltaListener listener)
			{
				robot2.addMessageListener(listener);
			}

			@Override
			public void removeMessageListener(RobotLocationDeltaListener listener)
			{
				robot2.removeMessageListener(listener);
			}

			@Override
			public double getPlatformRadius()
			{
				return robot2.getPlatformRadius();
			}

			@Override
			public void setStraight(String calledBy)
			{
				robot2.setStraight("MapBuilder 12");

			}
		};
	}

	void addMap(RobotPoseSource pose) throws InterruptedException
	{
		ProbabilityMapIIFc map = new SubMapBuilder().buildMap(robot);
		SubMapHolder currentSubMap = new SubMapHolder(new Pose(pose.getXyPosition().getX().convert(DistanceUnit.CM),
				pose.getXyPosition().getY().convert(DistanceUnit.CM), pose.getHeading()), map);
		subMaps.add(currentSubMap);

		CountDownLatch latch = new CountDownLatch(1);
		regernateWorld(world, latch);
		latch.await();
		if (particleFilterProxy != null)
		{
			// at startup the particle filter isn't created because we have to
			// wait for the map
			particleFilterProxy.updateMap(world);
		}
	}

	private void regernateWorld(final ProbabilityMapIIFc targetWorld, final CountDownLatch latch)
	{
		Runnable runner = new Runnable()
		{

			@Override
			public void run()
			{
				targetWorld.erase();

				for (SubMapHolder subMap : subMaps)
				{
					int minX = subMap.map.getMinX();
					int maxX = subMap.map.getMaxX();
					int minY = subMap.map.getMinY();
					int maxY = subMap.map.getMaxY();

					for (int x = minX; x < maxX + 1; x++)
					{
						for (int y = minY; y < maxY + 1; y++)
						{
							double value = subMap.map.get(x, y);
							Vector3D vector = new Vector3D(x, y, 0);
							double distance = vector.distance(Vector3D.ZERO);
							// nearest point is value 1, furthest is 0;
							double magnetude = Math.max(0, (1000 - distance) / 1000.0);
							// adjust magnetude to 0 to 0.5
							magnetude /= 2.0;

							Pose pose = subMap.getMapPose();

							vector = pose.applyTo(vector);

							double targetWorldValue = targetWorld.get((int) vector.getX(), (int) vector.getY());

							if (Math.abs(0.5 - targetWorldValue) < magnetude)

							{
								if (value < 0.5)
								{
									targetWorld.resetPoint((int) vector.getX(), (int) vector.getY());
									targetWorld.updatePoint((int) vector.getX(), (int) vector.getY(), Occupancy.VACANT,
											0.5 + magnetude, 1);
								} else if (value > 0.5)
								{
									targetWorld.resetPoint((int) vector.getX(), (int) vector.getY());
									targetWorld.updatePoint((int) vector.getX(), (int) vector.getY(),
											Occupancy.OCCUPIED, 0.5 + magnetude, 1);
								}
							}

						}
					}
				}

				File file = new File("maps/" + System.currentTimeMillis() + ".bmp");
				file.mkdirs();
				targetWorld.save(file);
				logger.error("World saved as " + file.getAbsolutePath());

				latch.countDown();
			}
		};

		new Thread(runner).start();
	}

	private boolean complete = false;

	public void update() throws InterruptedException
	{

		boolean hasReachedDestination = navigatorControl.hasReachedDestination();
		long targetElapsedMinutes = targetAge.elapsed(TimeUnit.MINUTES);
		long targetElapsedSeconds = targetAge.elapsed(TimeUnit.SECONDS);
		if (hasReachedDestination || (navigatorControl.isStuck() && targetElapsedSeconds > 120)
				|| targetElapsedMinutes > 2 || nextTarget != null)
		{
			navigatorControl.stop();

			if (isUnexplored(poseAdjuster.getXyPosition().getVector(DistanceUnit.CM), 0))
			{
				addMap(poseAdjuster);
			}

			if (nextTarget != null)
			{
				navigatorControl.calculateRouteTo((int) nextTarget.getX(), (int) nextTarget.getY(), null,
						RouteOption.ROUTE_THROUGH_CLEAR_SPACE_ONLY);
				nextTarget = null;
			} else
			{
				chooseTarget();
			}
			// navigateThroughSubMapsTo();
			navigatorControl.go();
		}
	}

	private void chooseTarget()
	{

		addMap = true;
		if (!setNewTarget())
		{
			addMap = false;
			if (subMaps.isEmpty())
			{
				navigatorControl.calculateRouteTo(0, 0, null, RouteOption.ROUTE_THROUGH_CLEAR_SPACE_ONLY);

			} else
			{
				SubMapHolder map = subMaps.get((int) (Math.random() * subMaps.size()));
				navigatorControl.calculateRouteTo((int) map.getMapPose().getX(), (int) map.getMapPose().getY(), null,
						RouteOption.ROUTE_THROUGH_CLEAR_SPACE_ONLY);

			}
		}
		targetAge.reset();
		targetAge.start();

		navigatorControl.go();
	}

	double closestToIdealDistance = Double.MAX_VALUE;

	boolean setNewTarget()
	{

		closestToIdealDistance = Double.MAX_VALUE;

		int minX = world.getMinX();
		int maxX = world.getMaxX() + 1;
		int minY = world.getMinY();
		int maxY = world.getMaxY() + 1;

		XY location = null;
		for (int x = minX; x < maxX; x += 10)
		{
			for (int y = minY; y < maxY; y += 10)
			{
				XY newLocation = new XY(x, y);

				boolean isVisited = false;
				for (XY visted : vistedLocations)
				{
					isVisited |= Math.abs(visted.getX() - newLocation.getX()) < MIN_TARGET_SEPARATION / 3.0
							&& Math.abs(visted.getY() - newLocation.getY()) < MIN_TARGET_SEPARATION / 3.0;
				}

				if (!isVisited)
				{
					if (isTarget(x, y))
					{
						location = newLocation;
					}
				}
			}
		}
		if (location != null)
		{
			vistedLocations.add(location);
			navigatorControl.calculateRouteTo(location.getX(), location.getY(), null,
					RouteOption.ROUTE_THROUGH_CLEAR_SPACE_ONLY);

			return true;
		}
		return false;
		// complete = true;
		// return false;
	}

	private boolean isTarget(int x, int y)
	{

		double idealDistance = MIN_TARGET_SEPARATION * 2.0;
		DistanceXY currentLocation = poseAdjuster.getXyPosition();
		if (x >= world.getMinX() && x <= world.getMaxX())
		{
			if (y >= world.getMinY() && y <= world.getMaxY())
			{
				if (checkIsValidRouteTarget(x, y))
				{

					Vector3D position = new Vector3D(x, y, 0);
					double distance = Vector3D.distance(currentLocation.getVector(DistanceUnit.CM), position);

					if (isUnexplored(position, 0))
					{

						if (Math.abs(idealDistance - distance) < closestToIdealDistance)
						{
							closestToIdealDistance = Math.abs(idealDistance - distance);
							return true;
						}
					}

				}
			}
		}
		return false;
	}

	private boolean checkIsValidRouteTarget(int x, int y)
	{
		double requiredValue = 0.25;
		boolean isValid = world.get(x, y) < requiredValue;
		if (isValid)
		{
			for (int checkRadius = 5; checkRadius < 25; checkRadius += 5)
			{
				isValid &= world.get(x + checkRadius, y) < requiredValue;
				isValid &= world.get(x - checkRadius, y) < requiredValue;
				isValid &= world.get(x, y + checkRadius) < requiredValue;
				isValid &= world.get(x, y - checkRadius) < requiredValue;
				isValid &= world.get(x + checkRadius, y + checkRadius) < requiredValue;
				isValid &= world.get(x - checkRadius, y - checkRadius) < requiredValue;
				isValid &= world.get(x + checkRadius, y - checkRadius) < requiredValue;
				isValid &= world.get(x - checkRadius, y + checkRadius) < requiredValue;
			}
		}
		return isValid;

	}

	boolean isUnexplored(Vector3D position, double heading)
	{
		int from = 0;
		int to = 360;
		int maxDistance = 1000;
		Particle particle = new Particle(position.getX(), position.getY(), heading, 0, 0);

		int existInMap = 0;

		double angleStepSize = 3.6;

		for (double h = from; h < to; h += angleStepSize)
		{

			if (particle.simulateObservation(world, h, maxDistance,
					RobotSimulator.REQUIRED_POINT_CERTAINTY) < maxDistance)
			{
				existInMap++;

			}
		}
		// there is too much unexplored (unmapped) in sight
		return existInMap < (Math.abs(to - from) / angleStepSize) * .9;

	}

	RobotPoseSource getZeroPose()
	{
		return new RobotPoseSource()
		{

			@Override
			public DistanceXY getXyPosition()
			{
				return new DistanceXY(0, 0, DistanceUnit.CM);
			}

			@Override
			public double getStdDev()
			{
				return 0;
			}

			@Override
			public double getHeading()
			{
				return 0;
			}

			@Override
			public DataSourcePoint getParticlePointSource()
			{
				return null;
			}

			@Override
			public DataSourceMap getHeadingMapDataSource()
			{
				return null;
			}

			@Override
			public ParticleFilterStatus getParticleFilterStatus()
			{
				return null;
			}
		};
	}

}
