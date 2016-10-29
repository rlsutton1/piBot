package au.com.rsutton.deeplearning.feature;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;

import au.com.rsutton.mapping.KitchenMapBuilder;
import au.com.rsutton.mapping.particleFilter.Particle;

public class FullWorldFeatureSimulator extends FeatureSimulatorBase
{
	public static final int ACUTE_CORNER = 2;

	double positive = 0;
	double negative = 0;

	@Test
	public void test1()
	{

		for (int i = 0; i < 10; i++)
		{
			Scan lineScan = getLineScan();
			System.out.println("Label " + lineScan.label);
			for (Vector3D vector : lineScan.points)
			{
				System.out.println(vector);

			}
			System.out.println("\n");
		}
	}

	public FullWorldFeatureSimulator(int dataSetSize)
	{
		super(ACUTE_CORNER, dataSetSize);

		KitchenMapBuilder.buildKitchenMap(world);
		long ctr = 0;
		while (cache.size() < dataSetSize)
		{
			ctr++;
			populateCache();
			if (ctr % 100 == 0)
			{
				System.out.println(positive + " " + negative + " " + cache.size());
			}
		}

	}

	Random rand = new Random();
	int pos = 0;

	@Override
	Scan innerGetLineScan()
	{
		pos++;
		pos = pos % cache.size();
		return cache.get(pos);
	}

	private Scan populateCache()
	{

		Scan scan = null;
		// this is what I'm working on right now!!!

		// choose random pose on world ->
		// choose random x,y on world
		// choose random orientation
		double heading = rand.nextInt(360);
		int x = rand.nextInt(world.getMaxX() - world.getMinX()) - world.getMinX();
		int y = rand.nextInt(world.getMaxY() - world.getMinY()) - world.getMinY();

		// perform scan

		// determine if scan is a feature or not

		// return scan

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
		if (obs.size() < 7)
		{
			obs.clear();
		} else
		{
			double label = 0;
			Vector3D vector = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(heading)).applyTo(obs.get(3))
					.add(new Vector3D(x, y, 0));

			for (Vector3D feature : world.getFeatures())
			{
				if (Math.abs(feature.getX() - vector.getX()) < 10 && Math.abs(feature.getY() - vector.getY()) < 10)
				{
					// it's a feature
					label = 1;
					break;
				}
			}

			if (label > 0.5)
			{
				positive++;
				scan = new Scan(label, obs);
				cache.add(scan);
			}
			if (label < 0.5 && positive / negative > 0.25)
			{
				negative++;
				scan = new Scan(label, obs);
				cache.add(scan);
			}
		}
		return scan;
	}

	@Override
	protected int getLabel()
	{
		return super.getLabel();
	}

}
