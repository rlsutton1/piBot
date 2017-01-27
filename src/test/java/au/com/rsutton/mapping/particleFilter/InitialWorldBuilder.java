package au.com.rsutton.mapping.particleFilter;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.mapping.probability.Occupancy;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.robot.RobotListener;
import au.com.rsutton.robot.rover.Angle;

public class InitialWorldBuilder implements RobotListener
{

	public static final double REQUIRED_POINT_CERTAINTY = 0.90;

	private ProbabilityMapIIFc world;

	volatile boolean done = false;
	volatile boolean hasFoundPoints = false;
	Double offset = null;

	Double heading = null;

	private volatile boolean suspended = false;

	public InitialWorldBuilder(ProbabilityMapIIFc map, RobotInterface robot)
	{
		world = map;

		try
		{
			robot.freeze(true);
			robot.publishUpdate();
			Thread.sleep(3000);

			robot.addMessageListener(this);

			Thread.sleep(30000);

			robot.removeMessageListener(this);

			robot.freeze(false);
			robot.publishUpdate();

		} catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void observed(RobotLocation observation)
	{
		if (suspended)
		{
			return;
		}
		Angle heading = observation.getDeadReaconingHeading();

		double adjustedHeading = (heading.getDegrees());

		Particle particle = new Particle(0, 0, adjustedHeading, 0, 0);
		Vector3D pos = new Vector3D(0, 0, 0);

		boolean hasNewPoints = false;
		for (ScanObservation obs : observation.getObservations())
		{

			Vector3D point = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(adjustedHeading))
					.applyTo(obs.getVector());
			if (point.getNorm() < 1000)
			{
				point = pos.add(point);

				clearPoints(pos, point);
				double simulateObservation = particle.simulateObservation(world, Math.toDegrees(obs.getAngleRadians()),
						1000, REQUIRED_POINT_CERTAINTY);
				if (simulateObservation == 1000)
				{
					hasFoundPoints = true;
					hasNewPoints = true;
					world.updatePoint((int) (point.getX()), (int) (point.getY()), Occupancy.OCCUPIED, 0.5,
							world.getBlockSize() / 2);
					System.out.println("Adding point to map");
				}
			} else
			{
				System.out.println("Rejecting very distant point");
			}
		}
		if (!hasNewPoints && observation.getObservations().size() > 5 && hasFoundPoints)
		{
			done = true;
		}
	}

	void clearPoints(Vector3D pos, Vector3D point)
	{
		// clear out points

		double x1 = pos.getX();
		double y1 = pos.getY();

		double x2 = point.getX();
		double y2 = point.getY();

		double dist = pos.distance(point) - world.getBlockSize();
		if (dist > 0)
		{
			for (int i = 0; i < dist; i++)
			{
				double percent = i / dist;
				double x = (percent * x2) + ((1.0 - percent) * x1);
				double y = (percent * y2) + ((1.0 - percent) * y1);
				if (world.get(x, y) < REQUIRED_POINT_CERTAINTY)
				{
					world.updatePoint((int) (x), (int) (y), Occupancy.VACANT, 0.05, 2);
				}
			}
		}
	}

}
