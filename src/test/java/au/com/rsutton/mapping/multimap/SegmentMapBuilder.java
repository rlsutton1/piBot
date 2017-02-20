package au.com.rsutton.mapping.multimap;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.mapping.particleFilter.Particle;
import au.com.rsutton.mapping.particleFilter.ParticleFilterIfc;
import au.com.rsutton.mapping.particleFilter.ScanObservation;
import au.com.rsutton.mapping.probability.Occupancy;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.navigation.NavigatorControl;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.robot.RobotListener;
import au.com.rsutton.ui.MapDrawingWindow;
import au.com.rsutton.ui.WrapperForObservedMapInMapUI;

public class SegmentMapBuilder implements RobotListener
{

	public static final double REQUIRED_POINT_CERTAINTY = 0.90;

	private ProbabilityMapIIFc world;

	volatile boolean done = false;
	volatile boolean hasFoundPoints = false;
	Double offset = null;

	Double heading = 0d;

	private volatile boolean suspended = false;

	private MapDrawingWindow panel;

	private double fixedX;

	private double fixedY;

	private double fixedHeading;

	/**
	 * this initialWorldBuilder expects the navigator to position the robot
	 * based on a map which is not passed in here. The map that is passed in
	 * here is expected to be an uninitialized map which will be built
	 * 
	 * @param map
	 * @param navigator
	 * @param robot
	 * @param x
	 * @param y
	 * @param heading
	 */
	public SegmentMapBuilder(ProbabilityMapIIFc map, NavigatorControl navigator, RobotInterface robot,
			ParticleFilterIfc activeParticleFilter)
	{

		world = map;

		panel = new MapDrawingWindow();

		panel.addDataSource(new WrapperForObservedMapInMapUI(map));

		try
		{

			Thread.sleep(2000);
			navigator.suspend();
			activeParticleFilter.suspend();

			robot.setSpeed(new Speed(new Distance(0, DistanceUnit.CM), Time.perSecond()));

			Thread.sleep(2000);
			robot.freeze(true);
			robot.publishUpdate();
			Thread.sleep(3000);

			this.fixedX = activeParticleFilter.dumpAveragePosition().getX();
			this.fixedY = activeParticleFilter.dumpAveragePosition().getY();
			this.fixedHeading = activeParticleFilter.getAverageHeading();

			robot.addMessageListener(this);
			Thread.sleep(20000);

			robot.removeMessageListener(this);
			activeParticleFilter.resume();

			navigator.resume();

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
		double adjustedHeading = fixedHeading;

		double cx = fixedX;
		double cy = fixedY;
		Particle particle = new Particle(cx, cy, adjustedHeading, 0, 0);
		Vector3D pos = new Vector3D(cx, cy, 0);

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
