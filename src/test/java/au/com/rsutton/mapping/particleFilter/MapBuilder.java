package au.com.rsutton.mapping.particleFilter;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.mapping.probability.Occupancy;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.ui.MapDrawingWindow;
import au.com.rsutton.ui.WrapperForObservedMapInMapUI;

public class MapBuilder implements ParticleFilterListener
{

	private MapDrawingWindow panel;

	ProbabilityMap world = new ProbabilityMap(10);

	private NavigatorControl navigatorControl;

	MapBuilder(ParticleFilterIfc particleFilter, NavigatorControl navigatorControl)
	{
		panel = new MapDrawingWindow();
		this.navigatorControl = navigatorControl;

		panel.addDataSource(new WrapperForObservedMapInMapUI(world));

		particleFilter.addListener(this);
	}

	int lastHeading = 0;

	private boolean complete = false;

	@Override
	public void update(Vector3D averagePosition, double averageHeading, double stdDev,
			ParticleFilterObservationSet particleFilterObservationSet)
	{

		if (stdDev < 30 && Math.abs(lastHeading - averageHeading) < 3)
		{
			Vector3D pos = averagePosition;

			for (ScanObservation obs : particleFilterObservationSet.getObservations())
			{

				Vector3D point = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(averageHeading)).applyTo(obs
						.getVector());
				point = pos.add(point);

				clearPoints(pos, point);
				world.updatePoint((int) (point.getX()), (int) (point.getY()), Occupancy.OCCUPIED, 0.85, 5);
			}

		}
		lastHeading = (int) averageHeading;

		if (doWeNeedToStopWhileScanningCatchesUp(averagePosition, averageHeading))
		{
			navigatorControl.stop();
		} else
		{
			navigatorControl.go();
		}

		if (navigatorControl.hasReachedDestination())
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
					for (int heading = 0; heading < 360; heading += 45)
					{
						Vector3D position = new Vector3D(x, y, 0);
						if (isUnexplored(position, heading, -25, 25, 1000))
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

		return isUnexplored(averagePosition, averageHeading, -70, 70, 1000);
	}

	boolean isUnexplored(Vector3D position, double heading, int from, int to, int maxDistance)
	{
		Particle particle = new Particle(position.getX(), position.getY(), heading, 0, 0);

		int existInMap = 0;

		for (double h = from; h < to; h += 5)
		{

			if (particle.simulateObservation(world, h, maxDistance) < maxDistance)
			{
				existInMap++;

			}
		}
		// there is too much unexplored (unmapped) in sight
		return existInMap < (Math.abs(to - from) / 5.0) * .5;
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
				world.updatePoint((int) (x), (int) (y), Occupancy.VACANT, 0.05, 2);
			}
		}
	}

	public boolean isComplete()
	{
		return complete;
	}
}
