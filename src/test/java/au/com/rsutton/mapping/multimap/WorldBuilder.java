package au.com.rsutton.mapping.multimap;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;

import com.google.common.base.Stopwatch;

import au.com.rsutton.mapping.multimap.Averager.Sample;
import au.com.rsutton.mapping.particleFilter.InitialWorldBuilder;
import au.com.rsutton.mapping.particleFilter.Particle;
import au.com.rsutton.mapping.particleFilter.ParticleFilterImpl;
import au.com.rsutton.mapping.particleFilter.Pose;
import au.com.rsutton.mapping.particleFilter.RobotImple;
import au.com.rsutton.mapping.particleFilter.StartPosition;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.navigation.Navigator;
import au.com.rsutton.navigation.router.RouteOption;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.ui.MapDrawingWindow;
import au.com.rsutton.ui.WrapperForObservedMapInMapUI;

public class WorldBuilder
{

	private MapDrawingWindow panel;
	private ProbabilityMapProxy map;
	private ParticleFilterProxy pf;
	private Navigator navigator;

	@Test
	public void build() throws InterruptedException
	{
		try
		{
			// work on multi-map
			RobotInterface robot = new RobotImple();
			// RobotInterface robot = new
			// RobotSimulator(KitchenMapBuilder.buildKitchenMap());

			int initialHeading = 0;

			// ((RobotSimulator) robot).setLocation(-150, 300, initialHeading);
			map = new ProbabilityMapProxy(new ProbabilityMap(5));

			panel = new MapDrawingWindow();

			panel.addDataSource(new WrapperForObservedMapInMapUI(map));

			new InitialWorldBuilder(map, robot);

			pf = new ParticleFilterProxy(new ParticleFilterImpl(map, 1000, 0.5, 0.5, StartPosition.USE_POSE, robot,
					new Pose(0, 0, initialHeading)));

			navigator = new Navigator(map, pf, robot);

			List<Vector3D> positionHistory = new LinkedList<>();

			while (setNewTarget())
			{
				Vector3D currentPosition = pf.dumpAveragePosition();
				navigator.go();

				Stopwatch timer = Stopwatch.createStarted();
				while (!navigator.hasReachedDestination() && timer.elapsed(TimeUnit.MINUTES) < 2)
				{
					Thread.sleep(1000);
					currentPosition = pf.dumpAveragePosition();
					positionHistory.add(currentPosition);
					while (Vector3D.distance(positionHistory.get(1), currentPosition) > 50
							&& positionHistory.size() > 2)
					{
						positionHistory.remove(0);
					}
				}
				if (navigator.hasReachedDestination())
				{

					ProbabilityMap nextMap = new ProbabilityMap(5);
					new SegmentMapBuilder(nextMap, navigator, robot, pf);

					Vector3D dumpAveragePosition = pf.dumpAveragePosition();
					Pose pose = new Pose(dumpAveragePosition.getX(), dumpAveragePosition.getY(),
							pf.getAverageHeading());

					ParticleFilterImpl newPf = new ParticleFilterImpl(nextMap, 1000, 0.5, 0.5, StartPosition.USE_POSE,
							robot, pose);
					PositionConsolidator consolidator = new PositionConsolidator(pf, newPf, navigator, currentPosition,
							positionHistory.get(0));

					newPf.shutdown();
					Sample offset = consolidator.getOffset();
					if (offset.accuracy < 5 && Math.abs(offset.x) < 10 && Math.abs(offset.y) < 10
							&& Math.abs(offset.heading) < 3)
					{

						MapMerger.mergeMaps(map, nextMap, consolidator.getOffset(), pose);
					}
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	LinkedHashSet<Long> previousLocations = new LinkedHashSet<>();

	boolean setNewTarget()
	{
		int xspread = Math.abs(map.getMaxX() - map.getMinX());
		int yspread = Math.abs(map.getMaxY() - map.getMinY());

		Random r = new Random();

		int ctr = 0;
		while (ctr < 300)
		{
			ctr++;
			int x = r.nextInt(xspread) + map.getMinX();
			int y = r.nextInt(yspread) + map.getMinY();

			// create unique location with a 50cm resolution
			long maxYWidth = 1000;
			long location = ((x / 50) * maxYWidth) + ((y / 50) - (maxYWidth / 2));
			if (!previousLocations.contains(location) && isTarget(x, y))
			{
				previousLocations.add(location);
				return true;
			}
			System.out.println(ctr);
		}

		return true;
		// complete = true;
		// return false;
	}

	private boolean isTarget(int x, int y)
	{
		if (x >= map.getMinX() && x <= map.getMaxX())
		{
			if (y >= map.getMinY() && y <= map.getMaxY())
			{
				if (checkIsValidRouteTarget(x, y))
				{

					Vector3D position = new Vector3D(x, y, 0);

					if (isUnexplored(position, 90))
					{
						navigator.calculateRouteTo(x, y, null, RouteOption.ROUTE_THROUGH_CLEAR_SPACE_ONLY);
						return true;
					}

				}
			}
		}
		return false;
	}

	private boolean checkIsValidRouteTarget(int x, int y)
	{
		double requiredValue = 0.25;
		boolean isValid = map.get(x, y) < requiredValue;
		if (isValid)
		{
			for (int checkRadius = map.getBlockSize(); checkRadius < 30; checkRadius += map.getBlockSize())
			{
				isValid &= map.get(x + checkRadius, y) < requiredValue;
				isValid &= map.get(x - checkRadius, y) < requiredValue;
				isValid &= map.get(x, y + checkRadius) < requiredValue;
				isValid &= map.get(x, y - checkRadius) < requiredValue;
				isValid &= map.get(x + checkRadius, y + checkRadius) < requiredValue;
				isValid &= map.get(x - checkRadius, y - checkRadius) < requiredValue;
				isValid &= map.get(x + checkRadius, y - checkRadius) < requiredValue;
				isValid &= map.get(x - checkRadius, y + checkRadius) < requiredValue;
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

			if (particle.simulateObservation(map, h, maxDistance,
					InitialWorldBuilder.REQUIRED_POINT_CERTAINTY) < maxDistance)
			{
				existInMap++;

			}
		}
		// there is too much unexplored (unmapped) in sight
		return existInMap < (Math.abs(to - from) / angleStepSize) * .9
				&& existInMap > (Math.abs(to - from) / angleStepSize) * .75;
	}

}
