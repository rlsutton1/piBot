package au.com.rsutton.deeplearning.feature;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.mapping.particleFilter.Particle;
import au.com.rsutton.mapping.probability.Occupancy;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;

public class FeatureSimulatorBase
{

	Random rand = new Random();
	ProbabilityMapIIFc world = new ProbabilityMap(10);

	int cacheSize = 20000;

	List<Scan> cache = new LinkedList<>();
	private final int label;

	FeatureSimulatorBase(int label, int cacheSize)
	{
		this.label = label;
		this.cacheSize = cacheSize;
	}

	class Scan
	{
		double label;
		List<Vector3D> points;

		Scan(double label, List<Vector3D> points)
		{
			this.label = label;
			this.points = points;
		}
	}

	protected int getLabel()
	{
		return label;
	}

	Scan getLineScan()
	{
		Scan result = innerGetLineScan();
		int ctr = 0;
		while (result == null || result.points.isEmpty())
		{
			result = innerGetLineScan();
			ctr++;
		}
		if (ctr > 10)
		{
			System.out.println(this.getClass().getSimpleName() + " Tried " + ctr + " times");

		}

		return result;
	}

	Scan innerGetLineScan()
	{
		int version = rand.nextInt(cacheSize);

		if (cache.size() > version)
		{
			return cache.get(version);
		}

		int y = rand.nextInt(500) + 10;
		int x = rand.nextInt(500) + 10;
		double heading = Math.toDegrees(Math.atan2(y, x)) - 270;
		Particle particle = new Particle(x, y, heading, 0, 0);

		List<Vector3D> obs = new LinkedList<>();

		for (double angle = -3 * 3; angle <= 3 * 3; angle += 3)
		{
			double distance = particle.simulateObservation(world, angle, 1000, 0.51);
			if (distance < 1000)
			{
				Vector3D vector = new Vector3D(0, distance, 0);
				vector = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(angle)).applyTo(vector);
				obs.add(vector);
			} else
			{
				obs.clear();
				break;
			}
		}
		Scan scan = null;
		if (obs.size() < 7)
		{
			obs.clear();
		} else
		{
			scan = new Scan(getLabel(), obs);
			cache.add(scan);
		}
		return scan;
	}

	public void drawLine(double x1, double y1, double x2, double y2)
	{

		world.drawLine(x1, y1, x2, y2, Occupancy.OCCUPIED, 1.0, 2);
	}
}
