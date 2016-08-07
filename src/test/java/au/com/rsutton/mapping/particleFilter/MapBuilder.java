package au.com.rsutton.mapping.particleFilter;

import java.awt.Color;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.navigation.RoutePlanner;
import au.com.rsutton.navigation.RoutePlanner.ExpansionPoint;
import au.com.rsutton.ui.MapDrawingWindow;

public class MapBuilder implements ParticleFilterListener
{

	private MapDrawingWindow panel;

	ProbabilityMap world = new ProbabilityMap(10);

	private NavigatorControl navigatorControl;

	MapBuilder(ParticleFilter particleFilter, NavigatorControl navigatorControl)
	{
		panel = new MapDrawingWindow();
		this.navigatorControl = navigatorControl;

		panel.addDataSource(world, new Color(255, 255, 255));

		particleFilter.addListener(this);
	}

	int lastHeading = 0;

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

				world.updatePoint((int) (point.getX()), (int) (point.getY()), 1.0, 2);
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
		RoutePlanner planner = new RoutePlanner(world);

		// plan route to current Location.
		planner.createRoute(currentX, currentY);

		// search route for furthest Point.
		ExpansionPoint farthestPoint = planner.getFurthestPoint();

		// less a few steps so we don't hit a wall
		for (int i = 0; i < 10; i++)
		{
			farthestPoint = planner.getRouteForLocation(farthestPoint.getX(), farthestPoint.getY());
		}

		navigatorControl.calculateRouteTo(farthestPoint.getX(), farthestPoint.getY(), 0);
	}

	boolean doWeNeedToStopWhileScanningCatchesUp(Vector3D averagePosition, double averageHeading)
	{

		Particle particle = new Particle(averagePosition.getX(), averagePosition.getY(), averageHeading, 0, 0);

		int existInMap = 0;

		for (double h = -70; h < 70; h += 5)
		{
			int maxDistance = 1000;
			if (particle.simulateObservation(world, h, maxDistance) < maxDistance)
			{
				existInMap++;

			}
		}
		// there is too much unexplored (unmapped) in sight
		return existInMap < 55;
	}

	void clearPoints(Vector3D pos, Vector3D point, ScanObservation obs)
	{
		// clear out points

		double x1 = pos.getX();
		double y1 = pos.getY();

		double x2 = point.getX();
		double y2 = point.getY();

		double dist = obs.getDisctanceCm();
		if (dist > 0)
		{
			for (int i = 0; i < dist; i++)
			{
				double percent = i / dist;
				double x = (percent * x1) + ((1.0 - percent) * x2);
				double y = (percent * y1) + ((1.0 - percent) * y2);
				world.updatePoint((int) (x), (int) (y), 0, 2);
			}
		}
	}
}
