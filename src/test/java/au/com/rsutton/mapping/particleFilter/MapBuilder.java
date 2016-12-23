package au.com.rsutton.mapping.particleFilter;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.pi4j.gpio.extension.lidar.LidarScanner;

import au.com.rsutton.mapping.probability.Occupancy;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.navigation.NavigatorControl;
import au.com.rsutton.ui.MapDrawingWindow;
import au.com.rsutton.ui.WrapperForObservedMapInMapUI;

public class MapBuilder implements ParticleFilterListener
{

	private MapDrawingWindow panel;

	ProbabilityMap world;

	private NavigatorControl navigatorControl;

	private ParticleFilterIfc particleFilter;

	private ScanReferenceChecker scanReferenceChecker;

	MapBuilder(ProbabilityMap map, ParticleFilterIfc particleFilter, NavigatorControl navigatorControl)
	{
		world = map;
		panel = new MapDrawingWindow();
		this.navigatorControl = navigatorControl;

		panel.addDataSource(new WrapperForObservedMapInMapUI(world));

		this.particleFilter = particleFilter;

		scanReferenceChecker = new ScanReferenceChecker();

		particleFilter.addListener(this);
	}

	int lastHeading = 0;

	private boolean complete = false;

	@Override
	public void update(Vector3D averagePosition, double averageHeading, double stdDev,
			ParticleFilterObservationSet particleFilterObservationSet)
	{

		scanReferenceChecker.check(particleFilter.getParticles(), new ObservationListener()
		{

			@Override
			public void useScan(ParticleFilterObservationSet observation, Pose pose)
			{
				Vector3D pos = new Vector3D(pose.getX(), pose.getY(), 0);

				for (ScanObservation obs : particleFilterObservationSet.getObservations())
				{

					Particle particle = new Particle(pose.getX(), pose.getY(), pose.getHeading(), 0, 0);

					Vector3D point = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(pose.getHeading()))
							.applyTo(obs.getVector());
					point = pos.add(point);

					clearPoints(pos, point);
					if (particle.simulateObservation(world, Math.toDegrees(obs.getAngleRadians()), 1000, 0.90) >= 1000)
					{
						world.updatePoint((int) (point.getX()), (int) (point.getY()), Occupancy.OCCUPIED, 0.5, 5);
					}
				}

			}
		});
		if (navigatorControl.isStopped())
		{

			particleFilter.addPendingScan(particleFilterObservationSet);

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

		if (navigatorControl.hasReachedDestination() || navigatorControl.isStuck())
		{
			setNewTarget((int) averagePosition.getX(), (int) averagePosition.getY());
		}
	}

	void setNewTarget(int currentX, int currentY)
	{

		int xspread = Math.abs(world.getMinX() - world.getMaxX());
		int yspread = Math.abs(world.getMinY() - world.getMaxY());

		for (int xo = 0; xo < xspread; xo += 10)
		{
			for (int yo = 0; yo < yspread; yo += 10)
			{
				int x = currentX + xo;
				int y = currentY + yo;
				if (isTarget(x, y))
				{
					return;
				}
				x = currentX - xo;
				y = currentY + yo;
				if (isTarget(x, y))
				{
					return;
				}
				x = currentX + xo;
				y = currentY - yo;
				if (isTarget(x, y))
				{
					return;
				}
				x = currentX - xo;
				y = currentY - yo;
				if (isTarget(x, y))
				{
					return;
				}
			}
		}

		complete = true;
	}

	Set<Vector3D> previousTargets = new HashSet<>();

	private boolean isTarget(int x, int y)
	{
		if (x >= world.getMinX() && x <= world.getMaxX())
		{
			if (y >= world.getMinY() && y <= world.getMaxY())
			{
				if (checkIsValidRouteTarget(x, y))
				{

					for (int h1 = 0; h1 < 360; h1 += 45)
					{
						int heading = lastHeading + h1;
						Vector3D position = new Vector3D(x, y, 0);
						if (isUnexplored(position, heading))
						{
							Vector3D target = new Vector3D(x, y, heading);
							if (!previousTargets.contains(target))
							{
								previousTargets.add(target);
								navigatorControl.calculateRouteTo(x, y, heading);
								return true;
							}
						}
						heading = lastHeading - h1;
						if (isUnexplored(position, heading))
						{
							Vector3D target = new Vector3D(x, y, heading);
							if (!previousTargets.contains(target))
							{
								previousTargets.add(target);
								navigatorControl.calculateRouteTo(x, y, heading);
								return true;
							}
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

	boolean doWeNeedToStopWhileScanningCatchesUp(Vector3D averagePosition, double averageHeading)
	{

		return isUnexplored(averagePosition, averageHeading);
	}

	boolean isUnexplored(Vector3D position, double heading)
	{
		int from = LidarScanner.MIN_ANGLE;
		int to = LidarScanner.MAX_ANGLE;
		int maxDistance = 1000;
		Particle particle = new Particle(position.getX(), position.getY(), heading, 0, 0);

		int existInMap = 0;

		double angleStepSize = 5;

		for (double h = from; h < to; h += angleStepSize)
		{

			if (particle.simulateObservation(world, h, maxDistance, 0.8) < maxDistance)
			{
				existInMap++;

			}
		}
		// there is too much unexplored (unmapped) in sight
		return existInMap < (Math.abs(to - from) / angleStepSize) * .9;
	}

	void clearPoints(Vector3D pos, Vector3D point)
	{
		// clear out points

		double x1 = pos.getX();
		double y1 = pos.getY();

		double x2 = point.getX();
		double y2 = point.getY();

		double dist = pos.distance(point) - 10;
		if (dist > 0)
		{
			for (int i = 0; i < dist; i++)
			{
				double percent = i / dist;
				double x = (percent * x2) + ((1.0 - percent) * x1);
				double y = (percent * y2) + ((1.0 - percent) * y1);
				double certainty = 0.05;
				if (world.get(x, y) > 0.5)
				{
					certainty = certainty * (1.0 - world.get(x, y));
				}
				if (world.get(x, y) < 0.90)
				{
					world.updatePoint((int) (x), (int) (y), Occupancy.VACANT, certainty, 2);
				}
			}
		}
	}

	public boolean isComplete()
	{
		return complete;
	}
}
