package au.com.rsutton.mapping.particleFilter;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import com.google.common.base.Stopwatch;

import au.com.rsutton.angle.AngleUtil;
import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.kalman.RobotPoseSourceNoop;
import au.com.rsutton.mapping.KitchenMapBuilder;
import au.com.rsutton.mapping.LoopMapBuilder;
import au.com.rsutton.mapping.XY;
import au.com.rsutton.mapping.multimap.ParticleFilterProxy;
import au.com.rsutton.mapping.probability.Occupancy;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.navigation.Navigator;
import au.com.rsutton.navigation.NavigatorControl;
import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.navigation.feature.RobotLocationDeltaListener;
import au.com.rsutton.navigation.graphslam.v3.GraphSlamConstraint;
import au.com.rsutton.navigation.graphslam.v3.GraphSlamNodeConstructor;
import au.com.rsutton.navigation.graphslam.v3.GraphSlamNodeImpl;
import au.com.rsutton.navigation.graphslam.v3.GraphSlamV3;
import au.com.rsutton.navigation.graphslam.v3.PoseWithMathOperators;
import au.com.rsutton.navigation.router.RouteOption;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.robot.RobotSimulator;
import au.com.rsutton.ui.CoordinateClickListener;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.ui.DataSourcePoint;
import au.com.rsutton.ui.MapDrawingWindow;
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

	private static final double DISTANCE_NOISE = 1;

	private static final double HEADING_NOISE = 1;

	private static final int CHANGE_COUNTER_ADD_MAP = 0;
	private static final int CHANGE_COUNTER_RESET = 10;
	private static final int CHANGE_COUNTER_SWAP_MAP = 5;

	double maxUsableDistance = 1000;

	Logger logger = LogManager.getLogger();

	private MapDrawingWindow panel;

	ProbabilityMapIIFc world = new ProbabilityMap(5);

	ProbabilityMapIIFc slamWorld = new ProbabilityMap(5);

	private NavigatorControl navigatorControl;

	private PoseAdjuster poseAdjuster;
	RobotInterface robot;

	Set<XY> vistedLocations = new HashSet<>();

	AtomicLong nodeSeed = new AtomicLong();

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
			if (node == null)
			{
				return mapPose;
			}
			// if (node.getConstraints().size() > 10)
			// {
			// return new Pose(node.getPosition().getX(),
			// node.getPosition().getY(), node.getPosition().getAngle());
			// }
			return mapPose;
		}

		// Pose getOriginalMapPose()
		// {
		// return mapPose;
		// }

		void setMapPose(Pose mapPose)
		{
			this.mapPose = mapPose;
		}

		private Pose mapPose;
		ProbabilityMapIIFc map;
		GraphSlamNodeImpl<PoseWithMathOperators> node;

		public Pose getSlamMapPose()
		{
			return new Pose(node.getPosition().getX(), node.getPosition().getY(), node.getPosition().getAngle());
		}
	}

	List<SubMapHolder> subMaps = new LinkedList<>();

	Stopwatch targetAge = Stopwatch.createStarted();

	private ParticleFilterProxy particleFilterProxy;

	SubMapHolder currentMap;

	GraphSlamV3<GraphSlamNodeImpl<PoseWithMathOperators>, PoseWithMathOperators> slam = new GraphSlamV3<>(
			getCtorPose());

	private PoseWithMathOperators createPoseValue(double x, double y, double angle)
	{
		return new PoseWithMathOperators(x, y, angle);
	}

	private GraphSlamNodeConstructor<GraphSlamNodeImpl<PoseWithMathOperators>, PoseWithMathOperators> getCtorPose()
	{
		return new GraphSlamNodeConstructor<GraphSlamNodeImpl<PoseWithMathOperators>, PoseWithMathOperators>()
		{

			@Override
			public PoseWithMathOperators zero()
			{
				return new PoseWithMathOperators(0, 0, 0);
			}

			@Override
			public GraphSlamNodeImpl<PoseWithMathOperators> construct(String name,
					PoseWithMathOperators initialPosition)
			{
				return new GraphSlamNodeImpl<>(name, initialPosition, zero());
			}
		};
	}

	Pose nextTarget = null;
	volatile boolean crashDetected = false;

	final boolean simulator = false;
	int maxSpeed = 50;

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
					robotS = new RobotSimulator(KitchenMapBuilder.buildKitchenMap());

					robotS.setLocation(-150, 100, new Random().nextInt(360));
				} else
				{
					robotS = new RobotSimulator(LoopMapBuilder.buildKitchenMap());

					robotS.setLocation(130, 50, new Random().nextInt(360));
				}
				this.robot = robotS;
			} else
			{
				this.robot = new RobotImple();
			}

			robot.addMessageListener(new RobotLocationDeltaListener()
			{

				@Override
				public void onMessage(Angle deltaHeading, Distance deltaDistance, List<ScanObservation> robotLocation,
						boolean bump)
				{
					if (bump)
					{
						crashDetected = true;
						System.out
								.println("bump *********************************************************************");
					}
				}
			});

			vistedLocations.add(new XY(0, 0));

			panel = new MapDrawingWindow("Map Builder (Clickable)", 0, 0, 1000);

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

			MapDrawingWindow slamPanel = new MapDrawingWindow("Slam map", 0, 600, 1000);
			slamPanel.addDataSource(new WrapperForObservedMapInMapUI(slamWorld));

			panel.addDataSource(new WrapperForObservedMapInMapUI(world));

			particleFilterProxy = new ParticleFilterProxy(null);
			this.poseAdjuster = new PoseAdjuster(new Pose(0, 0, 0), new RobotPoseSourceNoop(particleFilterProxy));

			addMap(getZeroPose());
			currentMap = subMaps.get(0);

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
						robot.setSpeed(new Speed(new Distance(-3, DistanceUnit.CM), Time.perSecond()));
						robot.turn(0);
						robot.publishUpdate();
						robot.freeze(false);
						TimeUnit.MILLISECONDS.sleep(100);
					}
					robot.freeze(true);
					robot.publishUpdate();
					crashDetected = false;
					chooseTarget();
					navigatorSuspended = false;
				}

				update();

				double x = poseAdjuster.getXyPosition().getX().convert(DistanceUnit.CM);
				double y = poseAdjuster.getXyPosition().getY().convert(DistanceUnit.CM);
				SubMapHolder nearestmap = findNearestSubMap(x, y);
				if (nearestmap != currentMap && nearestmap != null)
				{
					Vector3D nearest = new Vector3D(nearestmap.getMapPose().getX(), nearestmap.getMapPose().getY(), 0);
					Vector3D current = new Vector3D(currentMap.getMapPose().getX(), currentMap.getMapPose().getY(), 0);
					Vector3D here = new Vector3D(x, y, 0);
					if (here.distance(current) > here.distance(nearest) + 20 && changeCounter < CHANGE_COUNTER_SWAP_MAP)
					{
						slam.solve();
						for (SubMapHolder map : subMaps)
						{
							PoseWithMathOperators nodePosition = map.node.getPosition();
							Pose graphSlamPose = new Pose(nodePosition.getX(), nodePosition.getY(),
									nodePosition.getAngle());
							logger.error("Slam Pose: " + graphSlamPose + " actual pose -> " + map.getMapPose());

						}

						setupForSubMapTraversal(nearestmap);
						changeCounter = CHANGE_COUNTER_RESET;
					}
				}

				if (poseAdjuster != null && poseAdjuster.getParticleFilterStatus() == ParticleFilterStatus.LOCALIZED)
				{
					localized = true;
				}

				if (poseAdjuster != null && poseAdjuster.getParticleFilterStatus() == ParticleFilterStatus.POOR_MATCH
						&& changeCounter < CHANGE_COUNTER_ADD_MAP && localized == true)
				{
					// TODO:
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
					slam.solve();
					for (SubMapHolder map : subMaps)
					{
						PoseWithMathOperators nodePosition = map.node.getPosition();
						Pose graphSlamPose = new Pose(nodePosition.getX(), nodePosition.getY(),
								nodePosition.getAngle());
						logger.error("Slam Pose: " + graphSlamPose + " actual pose -> " + map.getMapPose());
					}

					for (SubMapHolder sm : subMaps)
					{
						PoseWithMathOperators nodePosition = sm.node.getPosition();
						Pose graphSlamPose = new Pose(nodePosition.getX(), nodePosition.getY(),
								nodePosition.getAngle());
						logger.error("*********** Slam Pose: " + graphSlamPose + " actual pose -> " + sm.getMapPose());
						logger.error("node: " + sm.node);
						for (GraphSlamConstraint<PoseWithMathOperators> c : sm.node.getConstraints())
						{
							logger.error("Node constraint: " + c);
						}

					}

					navigatorSuspended = false;
					localized = false;
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
			public void turn(double normalizeHeading)
			{
				if (!navigatorSuspended)
				{
					robot2.turn(normalizeHeading);
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
			public double getRadius()
			{
				return robot2.getRadius();
			}
		};
	}

	void addMap(RobotPoseSource pose) throws InterruptedException
	{
		ProbabilityMapIIFc map = new SubMapBuilder().buildMap(robot);
		SubMapHolder currentSubMap = new SubMapHolder(new Pose(pose.getXyPosition().getX().convert(DistanceUnit.CM),
				pose.getXyPosition().getY().convert(DistanceUnit.CM), pose.getHeading()), map);
		subMaps.add(currentSubMap);

		// TODO: angles are correct, X/Y are broken on slam calculations

		PoseWithMathOperators slamPose = createPoseValue(currentSubMap.getMapPose().getX(),
				currentSubMap.getMapPose().getY(), currentSubMap.getMapPose().getHeading());

		if (currentMap == null)
		{
			currentSubMap.node = slam.getRoot();
			// slam.addNode("nodeid-" + nodeSeed.incrementAndGet(), slamPose,
			// slamPose, 1,
			// slam.getRoot());

		} else
		{
			// get relative XY for map
			Vector3D xy = new Vector3D((currentSubMap.getMapPose().getX() - currentMap.getMapPose().getX()),
					(currentSubMap.getMapPose().getY() - currentMap.getMapPose().getY()), 0);

			// rotate the relative XY as if it were observed from the current
			// map
			Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0,
					Math.toRadians(currentMap.getMapPose().getHeading()));
			xy = rotation.applyInverseTo(xy);

			// create the pose object
			PoseWithMathOperators slamPoseOffset = createPoseValue(xy.getX(), xy.getY(),
					AngleUtil.delta(currentMap.getMapPose().getHeading(), currentSubMap.getMapPose().getHeading()));

			// add the node to slam
			currentSubMap.node = slam.addNode("nodeid-" + nodeSeed.incrementAndGet(), slamPose, slamPoseOffset, 1,
					currentMap.node);
		}
		slam.solve();
		for (SubMapHolder sm : subMaps)
		{
			PoseWithMathOperators nodePosition = sm.node.getPosition();
			Pose graphSlamPose = new Pose(nodePosition.getX(), nodePosition.getY(), nodePosition.getAngle());
			logger.error("*********** Slam Pose: " + graphSlamPose + " actual pose -> " + sm.getMapPose());
			logger.error("node: " + sm.node);
			for (GraphSlamConstraint<PoseWithMathOperators> c : sm.node.getConstraints())
			{
				logger.error("Node constraint: " + c);
			}

		}

		currentMap = currentSubMap;

		CountDownLatch latch = new CountDownLatch(2);
		regernateWorld(world, false, latch);
		regernateWorld(slamWorld, true, latch);
		latch.await();

		particleFilterProxy.changeParticleFilter(
				new ParticleFilterImpl(map, 1000, DISTANCE_NOISE, HEADING_NOISE, StartPosition.ZERO, robot, null));
		poseAdjuster.setPose(currentSubMap.getMapPose());

	}

	private void regernateWorld(final ProbabilityMapIIFc targetWorld, final boolean useSlam, final CountDownLatch latch)
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

							Pose pose;
							if (!useSlam)
							{
								pose = subMap.getMapPose();
							} else
							{
								pose = subMap.getSlamMapPose();
							}
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
				latch.countDown();
			}
		};

		new Thread(runner).start();
	}

	int lastHeading = 0;

	private boolean complete = false;

	private void setupForSubMapTraversal(SubMapHolder map) throws InterruptedException
	{
		navigatorSuspended = true;

		robot.freeze(true);
		robot.publishUpdate();

		boolean stable = false;
		double x = poseAdjuster.getXyPosition().getX().convert(DistanceUnit.CM);
		double y = poseAdjuster.getXyPosition().getY().convert(DistanceUnit.CM);
		double z = poseAdjuster.getHeading();
		while (!stable)
		{
			TimeUnit.MILLISECONDS.sleep(250);
			double x1 = poseAdjuster.getXyPosition().getX().convert(DistanceUnit.CM);
			double y1 = poseAdjuster.getXyPosition().getY().convert(DistanceUnit.CM);
			double z1 = poseAdjuster.getHeading();

			double totalDelta = Math.abs(x - x1) + Math.abs(y - y1) + Math.abs(HeadingHelper.getChangeInHeading(z, z1));
			z = z1;
			y = y1;
			x = x1;
			if (totalDelta < 5)
			{
				stable = true;
			} else
			{
				logger.error("Total Delta: " + totalDelta);
			}

		}

		final DistanceXY currentXY = poseAdjuster.getXyPosition();
		double currentHeading = poseAdjuster.getHeading();

		double initialHeading = currentHeading;

		// vector from current map origin to current position
		Vector3D vector1 = new Vector3D(currentXY.getX().convert(DistanceUnit.CM) - currentMap.getMapPose().getX(),
				currentXY.getY().convert(DistanceUnit.CM) - currentMap.getMapPose().getY(), 0);

		// calculate new post to initialise particle filter
		double angle1 = HeadingHelper.getChangeInHeading(currentHeading, map.getMapPose().heading);
		Vector3D pos = new Vector3D(currentXY.getX().convert(DistanceUnit.CM),
				currentXY.getY().convert(DistanceUnit.CM), 0);
		Pose pose = new Pose(map.getMapPose().applyInverseTo(pos).getX(), map.getMapPose().applyInverseTo(pos).getY(),
				angle1);

		particleFilterProxy.changeParticleFilter(new ParticleFilterImpl(map.map, 1000, DISTANCE_NOISE, HEADING_NOISE,
				StartPosition.USE_POSE, robot, pose));
		poseAdjuster.setPose(map.getMapPose());

		while (poseAdjuster.getParticleFilterStatus() == ParticleFilterStatus.LOCALIZING)
		{
			TimeUnit.MILLISECONDS.sleep(250);
			robot.freeze(true);
			robot.publishUpdate();
		}

		for (int ctr = 0; ctr < 3; ctr++)
		{
			TimeUnit.MILLISECONDS.sleep(100);
			robot.freeze(true);
			robot.publishUpdate();
		}

		// TODO:
		// DistanceXY newXY = currentXY;
		DistanceXY newXY = poseAdjuster.getXyPosition();

		// vector from newly localized position to origin of new map
		Vector3D vector2 = new Vector3D(map.getMapPose().getX() - newXY.getX().convert(DistanceUnit.CM),
				map.getMapPose().getY() - newXY.getY().convert(DistanceUnit.CM), 0);

		// get relative XY for map
		Vector3D xy = vector1.add(vector2);

		// rotate the relative XY as if it were observed from the current
		// map
		Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(currentMap.getMapPose().getHeading()));
		xy = rotation.applyInverseTo(xy);

		double finalHeading = poseAdjuster.getHeading();

		// create the pose object
		double deltaHeading = AngleUtil.delta(initialHeading, finalHeading);
		logger.error("Initial, final " + initialHeading + " " + finalHeading);
		logger.error("Delta heading is " + deltaHeading);
		double mapDelta = AngleUtil.delta(currentMap.getMapPose().getHeading(), map.getMapPose().getHeading());
		logger.error("map delta " + mapDelta);

		// TODO:
		double delta = AngleUtil.normalize(mapDelta - deltaHeading);

		logger.error("Final delta " + delta);
		PoseWithMathOperators slamPoseOffset = createPoseValue(xy.getX(), xy.getY(), delta);

		// add the node to slam

		double certainty = Math.min(0.75, 1.0 / Math.max(1.0, Math.abs(deltaHeading / 1.5)));
		logger.error("Certainty :" + certainty);

		currentMap.node.addConstraint(map.node, slamPoseOffset, certainty);

		currentMap = map;

		slam.solve();
		for (SubMapHolder sm : subMaps)
		{
			PoseWithMathOperators nodePosition = sm.node.getPosition();
			Pose graphSlamPose = new Pose(nodePosition.getX(), nodePosition.getY(), nodePosition.getAngle());
			logger.error("*********** Slam Pose: " + graphSlamPose + " actual pose -> " + sm.getMapPose());
			logger.error("node: " + sm.node);
			for (GraphSlamConstraint<PoseWithMathOperators> c : sm.node.getConstraints())
			{
				logger.error("Node constraint: " + c);
			}

		}

		CountDownLatch latch = new CountDownLatch(1);
		regernateWorld(slamWorld, true, latch);

		// TODO: one day we will want to wait when using the map for navigation

		// latch.await();

		navigatorSuspended = false;
	}

	private SubMapHolder findNearestSubMap(double x, double y)
	{
		double distance = Double.MAX_VALUE;

		Vector3D pos = new Vector3D(x, y, 0);
		SubMapHolder bestMap = null;
		for (SubMapHolder map : subMaps)
		{
			Vector3D mapPose = new Vector3D(map.getMapPose().x, map.getMapPose().y, 0);
			double dis = mapPose.distance(pos);
			if (dis < distance)
			{
				distance = dis;
				bestMap = map;
			}
		}

		return bestMap;
	}

	public void update() throws InterruptedException
	{

		boolean hasReachedDestination = navigatorControl.hasReachedDestination();
		long targetElapsedMinutes = targetAge.elapsed(TimeUnit.MINUTES);
		long targetElapsedSeconds = targetAge.elapsed(TimeUnit.SECONDS);
		if (hasReachedDestination || (navigatorControl.isStuck() && targetElapsedSeconds > 120)
				|| targetElapsedMinutes > 2 || nextTarget != null)
		{
			navigatorControl.stop();
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

	public boolean isComplete()
	{
		return complete;
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
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public DataSourceMap getHeadingMapDataSource()
			{
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ParticleFilterStatus getParticleFilterStatus()
			{
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

}
