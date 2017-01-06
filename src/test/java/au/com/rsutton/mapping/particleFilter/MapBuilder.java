package au.com.rsutton.mapping.particleFilter;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.base.Stopwatch;

import au.com.rsutton.mapping.probability.Occupancy;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.navigation.NavigatorControl;
import au.com.rsutton.navigation.router.RouteOption;
import au.com.rsutton.ui.MapDrawingWindow;
import au.com.rsutton.ui.WrapperForObservedMapInMapUI;

public class MapBuilder implements ParticleFilterListener
{

	double maxUsableDistance = 1000;
	double maxCertaintyDistance = 2000;

	private MapDrawingWindow panel;

	ProbabilityMap world;

	private NavigatorControl navigatorControl;

	private ParticleFilterIfc particleFilter;

	private ScanReferenceChecker scanReferenceChecker;

	Stopwatch targetAge = Stopwatch.createStarted();

	MapBuilder(ProbabilityMap map, ParticleFilterIfc particleFilter, NavigatorControl navigatorControl)
	{
		world = map;
		panel = new MapDrawingWindow();
		this.navigatorControl = navigatorControl;

		panel.addDataSource(new WrapperForObservedMapInMapUI(world));

		this.particleFilter = particleFilter;

		scanReferenceChecker = new ScanReferenceChecker();

		particleFilter.addListener(this);

		chooseTarget();
	}

	int lastHeading = 0;

	private boolean complete = false;

	private boolean returningHome = true;

	@Override
	public void update(Vector3D averagePosition, double averageHeading, double stdDev,
			ParticleFilterObservationSet particleFilterObservationSet)
	{

		// TODO: add a somewhat kalman fiter on the heading here!!!

		if (particleFilter.getStdDev() > 40)
		{
			// we're lost, the scan data is useless
			return;
		}

		if (particleFilter.getBestScanMatchScore() < 0.001)
		{
			return;
		}

		for (ScanObservation obs : particleFilterObservationSet.getObservations())
		{
			if (obs.getDisctanceCm() < 20)
			{

				// can't use observations closer that 30cm, the pariticle filter
				// gets confused and the data becomes bad

				// also we get some lidar artifacts at close range

				chooseTarget();

				return;
			}
		}

		for (ScanObservation obs : particleFilterObservationSet.getObservations())
		{

			Particle particle = new Particle(averagePosition.getX(), averagePosition.getY(), averageHeading, 0, 0);

			Vector3D point = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(averageHeading))
					.applyTo(obs.getVector());
			point = averagePosition.add(point);

			// calculate distance of the point observed
			double distance = Vector3D.distance(averagePosition, point);

			if (distance < maxUsableDistance)
			{
				// certainty of the observation is at max 0.5, and dimishes
				// to 0 at 10 meters(1000 cm)
				double certainty = ((maxCertaintyDistance - Math.min(maxCertaintyDistance, Math.max(0, distance)))
						/ maxCertaintyDistance) * 0.75;

				clearPoints(averagePosition, point);
				if (particle.simulateObservation(world, Math.toDegrees(obs.getAngleRadians()), 1000,
						InitialWorldBuilder.REQUIRED_POINT_CERTAINTY) >= 1000)
				{
					world.updatePoint((int) (point.getX()), (int) (point.getY()), Occupancy.OCCUPIED, 0.75,
							(int) particleFilter.getStdDev());
				}
			}
		}

		lastHeading = (int) averageHeading;

		// if (doWeNeedToStopWhileScanningCatchesUp(averagePosition,
		// averageHeading))
		// {
		// navigatorControl.stop();
		// } else
		// {
		// navigatorControl.go();
		// }

		navigatorControl.go();

		if (navigatorControl.hasReachedDestination() || particleFilter.getBestScanMatchScore() < 0.002
				|| (navigatorControl.isStuck() && targetAge.elapsed(TimeUnit.SECONDS) > 120)
				|| targetAge.elapsed(TimeUnit.MINUTES) > 2)
		{
			chooseTarget();
		}
	}

	private void chooseTarget()
	{
		if (targetAge.elapsed(TimeUnit.SECONDS) > 15)
		{
			if (returningHome)
			{
				if (setNewTarget())
				{
					returningHome = false;
				}
			} else
			{
				// return home every second move, to make sure we don't get lost
				navigatorControl.calculateRouteTo(0, 0, 0, RouteOption.ROUTE_THROUGH_CLEAR_SPACE_ONLY);
				returningHome = true;
			}
			targetAge.reset();
			targetAge.start();
		}
	}

	boolean setNewTarget()
	{
		int xspread = Math.abs(world.getMaxX() - world.getMinX());
		int yspread = Math.abs(world.getMaxY() - world.getMinY());

		Random r = new Random();

		int ctr = 0;
		while (ctr < 1000)
		{
			ctr++;
			int x = r.nextInt(xspread) + world.getMinX();
			int y = r.nextInt(yspread) + world.getMinY();
			if (isTarget(x, y))
			{

				return true;
			}
		}
		// }

		complete = true;
		return false;
	}

	private boolean isTarget(int x, int y)
	{
		if (x >= world.getMinX() && x <= world.getMaxX())
		{
			if (y >= world.getMinY() && y <= world.getMaxY())
			{
				if (checkIsValidRouteTarget(x, y))
				{

					int heading = lastHeading + 90;
					Vector3D position = new Vector3D(x, y, 0);
					if (isUnexplored(position, heading))
					{
						navigatorControl.calculateRouteTo(x, y, heading, RouteOption.ROUTE_THROUGH_CLEAR_SPACE_ONLY);
						return true;
					}
					heading = lastHeading - 90;
					if (isUnexplored(position, heading))
					{
						navigatorControl.calculateRouteTo(x, y, heading, RouteOption.ROUTE_THROUGH_CLEAR_SPACE_ONLY);
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
		boolean isValid = world.get(x, y) < requiredValue;
		int checkRadius = 30;
		isValid &= world.get(x + checkRadius, y) < requiredValue;
		isValid &= world.get(x - checkRadius, y) < requiredValue;
		isValid &= world.get(x, y + checkRadius) < requiredValue;
		isValid &= world.get(x, y - checkRadius) < requiredValue;
		isValid &= world.get(x + checkRadius, y + checkRadius) < requiredValue;
		isValid &= world.get(x - checkRadius, y - checkRadius) < requiredValue;
		isValid &= world.get(x + checkRadius, y - checkRadius) < requiredValue;
		isValid &= world.get(x - checkRadius, y + checkRadius) < requiredValue;

		return isValid;

	}

	// boolean doWeNeedToStopWhileScanningCatchesUp(Vector3D averagePosition,
	// double averageHeading)
	// {
	//
	// return isUnexplored(averagePosition, averageHeading);
	// }

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
					InitialWorldBuilder.REQUIRED_POINT_CERTAINTY) < maxDistance)
			{
				existInMap++;

			}
		}
		// there is too much unexplored (unmapped) in sight
		return existInMap < (Math.abs(to - from) / angleStepSize) * .9
				&& existInMap > (Math.abs(to - from) / angleStepSize) * .5;
	}

	void clearPoints(Vector3D pos, Vector3D point)
	{
		// clear out points

		double x1 = pos.getX();
		double y1 = pos.getY();

		double x2 = point.getX();
		double y2 = point.getY();

		double dist = pos.distance(point) - world.getBlockSize();

		if (dist > 0 && dist < maxUsableDistance)
		{
			// TODO: should inc i by the block size
			for (int i = 0; i < dist; i += world.getBlockSize())
			{

				// certainty of the observation is at max 0.5, and dimishes
				// to 0 at 10 meters(1000 cm)
				double certainty = ((maxCertaintyDistance - Math.min(maxCertaintyDistance, Math.max(0, i)))
						/ maxCertaintyDistance) * 0.75;

				double percent = i / dist;
				double x = (percent * x2) + ((1.0 - percent) * x1);
				double y = (percent * y2) + ((1.0 - percent) * y1);
				if (world.get(x, y) < InitialWorldBuilder.REQUIRED_POINT_CERTAINTY)
				{
					world.updatePoint((int) (x), (int) (y), Occupancy.VACANT, certainty, 2);
				} else
				{
					// don't clear beyond a definite point
					break;
				}
			}
		}
	}

	public boolean isComplete()
	{
		return complete;
	}
}
