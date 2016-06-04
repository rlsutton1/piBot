package au.com.rsutton.mapping.particleFilter;

import java.util.List;
import java.util.Random;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.mapping.probability.ProbabilityMap;

public class Particle
{

	double x;
	double y;
	double heading;
	private double totalVotes = 0.0;
	private double totalObservations;
	private double distanceNoise;
	private double headingNoise;

	public Particle(double x, double y, double heading, double distanceNoise, double headingNoise)
	{

		this.x = x;
		this.y = y;
		this.heading = heading;
		this.distanceNoise = distanceNoise;
		this.headingNoise = headingNoise;
	}

	public void move(ParticleUpdate update)
	{
		Random rand = new Random();

		// the amount of noise will affect the size of the point cloud
		// too little noise and it will fail to track

		double xn = rand.nextGaussian();
		double yn = rand.nextGaussian();
		double hn = rand.nextGaussian();

		double distance = update.getMoveDistance();

		double xNoise = Math.max(Math.abs(distance * xn * distanceNoise), Math.abs(xn * distanceNoise * 2.0))
				* Math.signum(xn);
		double yNoise = Math.max(Math.abs(distance * yn * distanceNoise), Math.abs(yn * distanceNoise * 2.0))
				* Math.signum(yn);
		double hNoise = Math.max(Math.abs(distance * hn * headingNoise), Math.abs(hn * headingNoise * 2.0))
				* Math.signum(hn);

		Vector3D unit = new Vector3D(0, 1, 0);
		Vector3D move = unit.scalarMultiply(distance);

		heading += update.getDeltaHeading();
		if (Math.abs(hNoise * update.getDeltaHeading()) > Math.abs(hNoise))
		{
			heading += hNoise * update.getDeltaHeading();
		} else
		{
			heading += hNoise;
		}

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
		x = newPosition.getX() + xNoise;
		y = newPosition.getY() + yNoise;

	}

	public void addObservation(ProbabilityMap currentWorld, ParticleFilterObservationSet data, double compassAdjustment)
	{
		if (data.getCompassHeading().getError() < 45)
		{

			double compassDiff = Math.abs(HeadingHelper.getChangeInHeading(data.getCompassHeading().getHeading()
					+ compassAdjustment, heading));
			if (compassDiff > 45)
			{
				// System.out.println("eliminating on compass heading " +
				// data.getCompassHeading().getHeading() + " "
				// + heading);
				totalObservations++;
				totalVotes -= 1000000;

			}
		} else
		{
			System.out.println("Good Value");
		}

		// max error is 30cm, after that we vote negative
		// error of 0 is a 1 vote
		// error or 30 is a 0 vote
		// error of 100 is a -3 vote
		double maxGoodError = 100.0;
		double maxGoodVote = 1.0;
		// double maxBadError = 100.0 - maxGoodError;
		// double maxBadVote = 3.0;

		List<ScanObservation> observations = data.getObservations();
		for (ScanObservation obs : observations)
		{
			double distance = obs.getDisctanceCm();
			double angle = Math.toDegrees(obs.getAngleRadians());
			double simDistance = simulateObservation(currentWorld, angle, distance + maxGoodError);// +
																									// maxBadError);

			// subtract world block size as this is the error caused by the
			// world block size, but stay > 0
			double error = Math.abs(simDistance - distance);

			double e = -2;
			if (error < maxGoodError)
			{
				e = ((maxGoodError - error) / maxGoodError) * maxGoodVote;
				e -= 0.5;
			}
			// else
			// {
			// double er = error - maxGoodError;
			// er = Math.min(maxBadError, er);
			// e = -(er / maxBadError) * maxBadVote;
			//
			// }

			totalVotes += e;
			totalObservations++;
			double currentRating = totalVotes / totalObservations;
			if (currentRating > 1.0)
			{
				System.out.println("error " + currentRating);

			}
		}

	}

	public double simulateObservation(ProbabilityMap currentWorld, double angle, double maxDistance)
	{
		Vector3D unit = new Vector3D(0, 1, 0);
		double hr = Math.toRadians(heading);
		Rotation rotation = new Rotation(RotationOrder.XYZ, 0.0, 0.0, hr + Math.toRadians(angle));
		unit = rotation.applyTo(unit);
		Vector3D location = new Vector3D(x, y, 0);
		// step size half the world block size
		int inc = Math.max(1, currentWorld.getBlockSize() / 4);
		for (int i = 0; i < maxDistance; i += 1)
		{
			double d = currentWorld.get(location.getX(), location.getY());
			if (d > 0.5)
			{
				return i;
			}
			location = location.add(unit);
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
		double rawRating = totalVotes / totalObservations;
		if (rawRating > 1.0)
		{
			System.out.println("Bad rating");
		}
		return Math.max(0.01, rawRating);

	}

	public int getSampleCount()
	{
		return (int) totalObservations;

	}

}
