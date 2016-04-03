package au.com.rsutton.mapping.particleFilter;

import java.util.List;
import java.util.Random;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.robot.rover.LidarObservation;

public class Particle
{

	double x;
	double y;
	double heading;
	private double totalError = 0.5;
	private double totalObservations;

	public Particle(double x, double y, double heading)
	{
		this.x = x;
		this.y = y;
		this.heading = heading;
	}

	public void move(ParticleUpdate update)
	{
		Random rand = new Random();

		// the amount of noise will affect the size of the point cloud
		// too little noise and it will fail to track
		double xNoise = rand.nextGaussian() * 2.0;
		double hNoise = rand.nextGaussian() * 2.0;

		double distance = update.getMoveDistance();

		distance += (distance + (xNoise * distance)) + (xNoise);

		Vector3D unit = new Vector3D(0, 1, 0);
		Vector3D move = unit.scalarMultiply(distance);

		// y += (update.getDeltaY() + (yNoise * update.getDeltaY())) + (yNoise *
		// 2.0);

		heading += (update.getDeltaHeading() +
		// (hNoise * update.getDeltaHeading())) +
		hNoise);
		if (heading < 0)
		{
			heading += 360.0;
		}
		if (heading > 360)
		{
			heading -= 360.0;
		}

		Vector3D position = new Vector3D(x, y, 0);
		Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(heading));

		Vector3D newPosition = rotation.applyTo(move).add(position);
		x = newPosition.getX();
		y = newPosition.getY();

	}

	public void addObservation(ProbabilityMap currentWorld, RobotLocation data)
	{
		if (data.getCompassHeading().getError() < 45)
		{
			if (Math.abs(HeadingHelper.getChangeInHeading(data.getCompassHeading().getHeading(), heading)) > 90)
			{
				System.out.println("eliminating on compass heading " + data.getCompassHeading().getHeading() + " "
						+ heading);
				totalObservations++;
				totalError += 1000000;
			}
		}
		List<LidarObservation> observations = data.getObservations();
		double totalSimDistanceError = 0.0;
		for (LidarObservation obs : observations)
		{
			double distance = obs.getDisctanceCm();
			double angle = Math.toDegrees(obs.getAngleRadians());
			double simDistance = simulateObservation(currentWorld, angle, distance * 2.0);
			totalSimDistanceError += Math.pow(Math.abs(simDistance - distance), 2);
			if (totalSimDistanceError > 20)
			{
//				System.out.println("Error here angel " + angle + " " + simDistance + " " + distance);
			}
			totalObservations++;
		}

		totalError += totalSimDistanceError;

	}

	public double simulateObservation(ProbabilityMap currentWorld, double angle, double maxDistance)
	{
		Vector3D unit = new Vector3D(0, 1, 0);
		double hr = Math.toRadians(heading);
		Rotation rotation = new Rotation(RotationOrder.XYZ, 0.0, 0.0, hr + Math.toRadians(angle));
		unit = rotation.applyTo(unit);
		Vector3D location = new Vector3D(x, y, 0);
		for (int i = 1; i < maxDistance; i++)
		{
			location = location.add(unit);
			double d = currentWorld.get(location.getX(), location.getY());
			if (d > 0.5)
			{
				return i;
			}
		}
		return maxDistance;
	}

	public double getX()
	{
		return x;
	}

	public double getY()
	{
		return y;
	}

	public double getHeading()
	{
		return heading;
	}

	/**
	 * between 0 and 1
	 * 
	 * @return
	 */
	public double getRating()
	{
		return Math.max(0.01, 1.0 - ((totalError / totalObservations) / 3000));

	}

}
