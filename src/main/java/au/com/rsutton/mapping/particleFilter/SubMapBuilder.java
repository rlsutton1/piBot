package au.com.rsutton.mapping.particleFilter;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.base.Stopwatch;

import au.com.rsutton.mapping.probability.Occupancy;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.navigation.feature.RobotLocationDeltaListener;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.robot.RobotSimulator;
import au.com.rsutton.ui.MapDrawingWindow;
import au.com.rsutton.ui.WrapperForObservedMapInMapUI;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.Speed;

public class SubMapBuilder implements RobotLocationDeltaListener
{

	double maxUsableDistance = 1000;

	private MapDrawingWindow panel;

	ProbabilityMapIIFc world = new ProbabilityMap(5);

	Stopwatch targetAge = Stopwatch.createStarted();

	SubMapBuilder() throws InterruptedException
	{
		panel = new MapDrawingWindow("Sub Map Builder", 0, 600, 250);

		panel.addDataSource(new WrapperForObservedMapInMapUI(world));
		panel.dispose();
	}

	private int scansRemaining = 6;

	ProbabilityMapIIFc buildMap(RobotInterface robot) throws InterruptedException
	{
		robot.freeze(true);
		robot.setSpeed(Speed.ZERO);
		robot.turn(0);

		robot.publishUpdate();
		TimeUnit.MILLISECONDS.sleep(500);
		robot.addMessageListener(this);

		while (scansRemaining > 0)
		{
			TimeUnit.MILLISECONDS.sleep(100);
			robot.freeze(true);
			robot.setSpeed(Speed.ZERO);
			robot.turn(0);

			robot.publishUpdate();
		}

		robot.freeze(false);
		robot.publishUpdate();

		robot.removeMessageListener(this);

		cleanupMap();
		panel.dispose();
		return world;

	}

	private void cleanupMap()
	{
		// force values to 1 or zero
		// do some smoothing.

		int minX = world.getMinX();
		int maxX = world.getMaxX();
		int minY = world.getMinY();
		int maxY = world.getMaxY();

		int gausianRadius = 2;

		for (Vector3D point : perimiter)
		{
			world.updatePoint((int) (point.getX()), (int) (point.getY()), Occupancy.OCCUPIED, 0.75, gausianRadius);

		}

		for (int x = minX; x < maxX + 1; x++)
		{
			for (int y = minY; y < maxY + 1; y++)
			{
				double value = world.get(x, y);
				if (value < 0.5)
				{
					world.resetPoint(x, y);
					world.updatePoint(x, y, Occupancy.VACANT, 1, 1);
				} else if (value > 0.5)
				{
					world.resetPoint(x, y);
					world.updatePoint(x, y, Occupancy.OCCUPIED, 1, 1);
				}
			}
		}

	}

	int lastHeading = 0;

	List<Vector3D> perimiter = new LinkedList<>();

	@Override
	public void onMessage(Angle deltaHeading, Distance deltaDistance, List<ScanObservation> observations, boolean bump)
	{
		if (scansRemaining > 0)
		{
			int gausianRadius = 2;
			Vector3D lastPoint = null;
			for (ScanObservation obs : observations)
			{

				Vector3D point = obs.getVector();

				// calculate distance of the point observed
				double distance = Vector3D.distance(Vector3D.ZERO, point);

				if (distance < maxUsableDistance)
				{
					clearPoints(Vector3D.ZERO, point);
					perimiter.add(point);
					world.updatePoint((int) (point.getX()), (int) (point.getY()), Occupancy.OCCUPIED, 0.75,
							gausianRadius);
				}

				if (lastPoint == null)
				{
					lastPoint = point;
				}
				double p2p = Vector3D.distance(point, lastPoint);
				if (p2p > 1 && p2p < 20)
				{
					// draw a line between the to endpoints
					double x1 = point.getX();
					double x2 = lastPoint.getX();
					double y1 = point.getY();
					double y2 = lastPoint.getY();

					// interpolate
					for (double i = 0; i < 1; i += 0.1)
					{
						double x = (x1 * i) + (x2 * (1.0 - i));
						double y = (y1 * i) + (y2 * (1.0 - i));
						world.updatePoint((int) (x), (int) (y), Occupancy.OCCUPIED, 0.75, gausianRadius);
					}

				}

				lastPoint = point;
			}
			scansRemaining--;
		}
	}

	void clearPoints(Vector3D pos, Vector3D point)
	{
		// clear out points

		Random rand = new Random();

		double x1 = pos.getX();
		double y1 = pos.getY();

		double x2 = point.getX();
		double y2 = point.getY();

		double dist = pos.distance(point) - world.getBlockSize();

		if (dist > 0 && dist < maxUsableDistance)
		{
			for (int i = 0; i < dist; i += world.getBlockSize())
			{
				double percent = i / dist;
				// certainty of the observation is at max 0.5, and dimishes
				// to 0 at 10 meters(1000 cm)
				double certainty = 1.0 - Math.sqrt(percent);
				certainty *= 0.3;

				double x = (percent * x2) + ((1.0 - percent) * x1);
				double y = (percent * y2) + ((1.0 - percent) * y1);
				if (world.get(x, y) < RobotSimulator.REQUIRED_POINT_CERTAINTY)
				{
					world.updatePoint((int) (x), (int) (y), Occupancy.VACANT, certainty, 2);
				} else
				{
					// check probability clearning
					double randomValue = rand.nextDouble();
					if (randomValue - 0.25 > percent)
					{
						// rand is greater than percentage of distace to the
						// detected point, so clear
						world.updatePoint((int) (x), (int) (y), Occupancy.VACANT, certainty, 2);
					}
					// don't clear beyond a definite point
					break;
				}
			}
		}
	}

}
