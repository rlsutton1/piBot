package au.com.rsutton.mapping.particleFilter;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;

import com.google.common.base.Stopwatch;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.hazelcast.DataLogValue;
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
import au.com.rsutton.navigation.router.ExpansionPoint;
import au.com.rsutton.navigation.router.RouteOption;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.robot.RobotSimulator;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.ui.DataSourcePoint;
import au.com.rsutton.ui.MapDrawingWindow;
import au.com.rsutton.ui.WrapperForObservedMapInMapUI;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;

public class MapBuilder
{

	private static final int RANGE_LIMIT_FOR_ADD = 150;

	private static final int MIN_TARGET_SEPARATION = (int) (RANGE_LIMIT_FOR_ADD * 0.4);

	private static final double DISTANCE_NOISE = 3;

	private static final double HEADING_NOISE = 3;

	double maxUsableDistance = 1000;

	private MapDrawingWindow panel;

	ProbabilityMapIIFc world = new ProbabilityMap(5);

	private NavigatorControl navigatorControl;

	private PoseAdjuster poseAdjuster;
	RobotInterface robot;

	Set<XY> vistedLocations = new HashSet<>();

	class SubMapHolder
	{
		public SubMapHolder(Pose pose, ProbabilityMapIIFc map)
		{
			this.mapPose = pose;
			this.map = map;
		}

		Pose mapPose;
		ProbabilityMapIIFc map;
	}

	List<SubMapHolder> subMaps = new LinkedList<>();

	Stopwatch targetAge = Stopwatch.createStarted();

	private ParticleFilterProxy particleFilterProxy;

	SubMapHolder currentMap;

	@Test
	public void test() throws InterruptedException
	{
		try
		{

			new DataWindow();

			boolean sim = false;

			if (sim)
			{
				// RobotSimulator robotS = new
				// RobotSimulator(KitchenMapBuilder.buildKitchenMap());

				// robotS.setLocation(-150, 100, new Random().nextInt(360));

				RobotSimulator robotS = new RobotSimulator(LoopMapBuilder.buildKitchenMap());

				robotS.setLocation(130, 50, new Random().nextInt(360));
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
						System.out
								.println("bump *********************************************************************");
					}
				}
			});

			vistedLocations.add(new XY(0, 0));

			panel = new MapDrawingWindow("Map Builder", 0, 0);

			panel.addDataSource(new WrapperForObservedMapInMapUI(world));

			particleFilterProxy = new ParticleFilterProxy(null);
			this.poseAdjuster = new PoseAdjuster(new Pose(0, 0, 0), particleFilterProxy);

			addMap(getZeroPose());
			currentMap = subMaps.get(0);

			this.navigatorControl = new Navigator(world, poseAdjuster, robot);

			Thread.sleep(20000);

			chooseTarget();

			int changeCounter = 10;
			boolean localized = false;
			while (!complete)
			{
				TimeUnit.MILLISECONDS.sleep(500);
				update();

				new DataLogValue("PF best raw score:", "" + poseAdjuster.getBestRawScore()).publish();

				if (poseAdjuster != null && poseAdjuster.getParticleFilterStatus() == ParticleFilterStatus.LOCALIZED)
				{
					localized = true;
				}

				if (poseAdjuster != null && poseAdjuster.getParticleFilterStatus() == ParticleFilterStatus.POOR_MATCH
						&& changeCounter < 0 && localized == true)
				{
					// TODO:
					navigatorControl.suspend();
					for (int i = 0; i < 15; i++)
					{
						robot.freeze(true);
						TimeUnit.MILLISECONDS.sleep(100);
					}
					addMap(poseAdjuster);
					navigatorControl.resume();
					localized = false;
					changeCounter = 10;

				}

				double x = poseAdjuster.getXyPosition().getX().convert(DistanceUnit.CM);
				double y = poseAdjuster.getXyPosition().getY().convert(DistanceUnit.CM);
				SubMapHolder nearestmap = findNearestSubMap(x, y);
				if (nearestmap != currentMap && nearestmap != null)
				{
					Vector3D nearest = new Vector3D(nearestmap.mapPose.getX(), nearestmap.mapPose.getY(), 0);
					Vector3D current = new Vector3D(currentMap.mapPose.getX(), currentMap.mapPose.getY(), 0);
					Vector3D here = new Vector3D(x, y, 0);
					double separation = nearest.distance(current);
					if (here.distance(current) > here.distance(nearest) + 20 && changeCounter < 0)
					{
						setupForSubMapTraversal(nearestmap);
						changeCounter = 10;
					}
				}
				changeCounter--;

			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	void addMap(RobotPoseSource pose) throws InterruptedException
	{
		ProbabilityMapIIFc map = new SubMapBuilder().buildMap(robot);
		SubMapHolder currentSubMap = new SubMapHolder(new Pose(pose.getXyPosition().getX().convert(DistanceUnit.CM),
				pose.getXyPosition().getY().convert(DistanceUnit.CM), pose.getHeading()), map);
		subMaps.add(currentSubMap);
		currentMap = currentSubMap;

		world.erase();

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
					if (vector.distance(Vector3D.ZERO) < RANGE_LIMIT_FOR_ADD)
					{
						vector = subMap.mapPose.applyTo(vector);

						if (world.get((int) vector.getX(), (int) vector.getY()) == 0.5)
						{
							if (value < 0.5)
							{
								world.resetPoint((int) vector.getX(), (int) vector.getY());
								world.updatePoint((int) vector.getX(), (int) vector.getY(), Occupancy.VACANT, 1, 1);
							} else if (value > 0.5)
							{
								world.resetPoint((int) vector.getX(), (int) vector.getY());
								world.updatePoint((int) vector.getX(), (int) vector.getY(), Occupancy.OCCUPIED, 1, 1);
							}
						}
					}
				}
			}
		}

		particleFilterProxy.changeParticleFilter(
				new ParticleFilterImpl(map, 1000, DISTANCE_NOISE, HEADING_NOISE, StartPosition.ZERO, robot, null));
		poseAdjuster.setPose(currentSubMap.mapPose);

	}

	int lastHeading = 0;

	private boolean complete = false;

	void navigateThroughSubMapsTo() throws InterruptedException
	{

		List<SubMapHolder> mapList = new LinkedList<>();
		Set<SubMapHolder> usedMaps = new HashSet<>();

		double x = poseAdjuster.getXyPosition().getX().convert(DistanceUnit.CM);
		double y = poseAdjuster.getXyPosition().getY().convert(DistanceUnit.CM);

		int targetX = 0;
		int targetY = 0;

		for (int i = 0; i < 600; i++)
		{
			ExpansionPoint next = navigatorControl.getRouteForLocation((int) x, (int) y);

			SubMapHolder map = findNearestSubMap(x, y);

			if (!usedMaps.contains(map))
			{
				mapList.add(map);
				usedMaps.add(map);
			}

			double dx = (x - next.getX()) * 5;
			x -= dx;
			double dy = (y - next.getY()) * 5;
			y -= dy;
			if (dx == 0 && dy == 0)
			{
				// reached the target
				targetX = (int) x;
				targetY = (int) y;
				break;
			}
		}

		for (int i = 0; i < mapList.size() - 1; i++)
		{
			SubMapHolder map = mapList.get(i);

			setupForSubMapTraversal(map);

			navigatorControl.calculateRouteTo((int) mapList.get(i + 1).mapPose.x, (int) mapList.get(i + 1).mapPose.y,
					null, RouteOption.ROUTE_THROUGH_CLEAR_SPACE_ONLY);
			TimeUnit.MILLISECONDS.sleep(1500);
			navigatorControl.go();
			while (!navigatorControl.hasReachedDestination())
			{
				TimeUnit.MILLISECONDS.sleep(500);
			}
		}

		if (mapList.size() > 1)
		{
			SubMapHolder map = mapList.get(mapList.size() - 1);
			setupForSubMapTraversal(map);
		}
		navigatorControl.calculateRouteTo(targetX, targetY, null, RouteOption.ROUTE_THROUGH_CLEAR_SPACE_ONLY);
		navigatorControl.go();

	}

	private void setupForSubMapTraversal(SubMapHolder map) throws InterruptedException
	{
		navigatorControl.suspend();
		TimeUnit.SECONDS.sleep(1);

		DistanceXY currentXY = poseAdjuster.getXyPosition();
		double currentHeading = poseAdjuster.getHeading();
		double tx = map.mapPose.getX() - currentXY.getX().convert(DistanceUnit.CM);
		double ty = map.mapPose.getY() - currentXY.getY().convert(DistanceUnit.CM);
		double th = HeadingHelper.normalizeHeading(currentHeading - map.mapPose.heading);

		Pose pose = new Pose(tx, ty, th);

		particleFilterProxy.changeParticleFilter(new ParticleFilterImpl(map.map, 1000, DISTANCE_NOISE, HEADING_NOISE,
				StartPosition.USE_POSE, robot, pose));
		poseAdjuster.setPose(map.mapPose);
		TimeUnit.SECONDS.sleep(1);

		while (poseAdjuster.getParticleFilterStatus() == ParticleFilterStatus.LOCALIZING)
		{
			TimeUnit.MILLISECONDS.sleep(250);
		}

		navigatorControl.resume();
	}

	private SubMapHolder findNearestSubMap(double x, double y)
	{
		double distance = Double.MAX_VALUE;

		Vector3D pos = new Vector3D(x, y, 0);
		SubMapHolder bestMap = null;
		for (SubMapHolder map : subMaps)
		{
			Vector3D mapPose = new Vector3D(map.mapPose.x, map.mapPose.y, 0);
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
				|| targetElapsedMinutes > 2)
		{
			navigatorControl.stop();
			for (int i = 0; i < 15; i++)
			{
				robot.freeze(true);
				TimeUnit.MILLISECONDS.sleep(100);
			}
			addMap(poseAdjuster);

			chooseTarget();

			// navigateThroughSubMapsTo();
			navigatorControl.go();
		}
	}

	private void chooseTarget()
	{

		if (!setNewTarget())
		{

			if (subMaps.isEmpty())
			{
				navigatorControl.calculateRouteTo(0, 0, null, RouteOption.ROUTE_THROUGH_CLEAR_SPACE_ONLY);

			} else
			{
				SubMapHolder map = subMaps.get((int) (Math.random() * subMaps.size()));
				navigatorControl.calculateRouteTo((int) map.mapPose.getX(), (int) map.mapPose.getY(), null,
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
			public void shutdown()
			{
			}

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
			public Double getBestScanMatchScore()
			{
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Double getBestRawScore()
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
