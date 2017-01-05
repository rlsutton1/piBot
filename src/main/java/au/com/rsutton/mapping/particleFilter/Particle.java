package au.com.rsutton.mapping.particleFilter;

import java.util.LinkedList;
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
	Random rand = new Random();

	List<ScanReference> scanReferences = new LinkedList<>();

	public Particle(double x, double y, double heading, double distanceNoise, double headingNoise)
	{

		this.x = x;
		this.y = y;
		this.heading = heading;
		this.distanceNoise = distanceNoise;
		this.headingNoise = headingNoise;
	}

	public Particle(Particle selectedParticle)
	{
		this.x = selectedParticle.x;
		this.y = selectedParticle.y;
		this.heading = selectedParticle.heading;
		this.distanceNoise = selectedParticle.distanceNoise;
		this.headingNoise = selectedParticle.headingNoise;

		// inherit scanReferences from the selectedParticle
		scanReferences.addAll(selectedParticle.scanReferences);

	}

	public List<ScanReference> getScanReferences()
	{
		return scanReferences;
	}

	public void move(ParticleUpdate update)
	{
		double distance = update.getMoveDistance();

		Vector3D unit = new Vector3D(0, 1, 0);
		Vector3D move = unit.scalarMultiply(distance);

		heading += update.getDeltaHeading();

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
		addNoise(Math.abs(distance));

	}

	private void addNoise(double distanceTravelled)
	{

		// the amount of noise will affect the size of the point cloud
		// too little noise and it will fail to track

		double xn = rand.nextGaussian();
		double yn = rand.nextGaussian();
		double hn = rand.nextGaussian();

		double xNoise = Math.max(Math.abs(distanceTravelled * xn * distanceNoise), Math.abs(xn * distanceNoise * 2.0))
				* Math.signum(xn);
		double yNoise = Math.max(Math.abs(distanceTravelled * yn * distanceNoise), Math.abs(yn * distanceNoise * 2.0))
				* Math.signum(yn);
		double hNoise = hn * headingNoise;

		x += xNoise;
		y += yNoise;
		heading += hNoise;

	}

	public void addObservation(ProbabilityMap currentWorld, ParticleFilterObservationSet data, double compassAdjustment,
			boolean useCompass)
	{
		if (useCompass && data.getCompassHeading().getError() < 45)
		{

			double compassDiff = Math.abs(HeadingHelper
					.getChangeInHeading(data.getCompassHeading().getHeading() + compassAdjustment, heading));
			if (compassDiff > 45)
			{
				// System.out.println("eliminating on compass heading " +
				// data.getCompassHeading().getHeading() + " "
				// + heading);
				totalObservations++;
				totalVotes -= 1000000;

			}
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
			double simDistance = simulateObservation(currentWorld, angle, distance + maxGoodError,
					InitialWorldBuilder.REQUIRED_POINT_CERTAINTY);// +
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

	/**
	 * scans upto maxDistance, returning the distance of the strongest match
	 * 
	 * @param currentWorld
	 * @param angle
	 * @param maxDistance
	 * @param occupancyThreshold
	 * @return
	 */
	public double simulateObservation(ProbabilityMap currentWorld, double angle, double maxDistance,
			double occupancyThreshold)
	{
		Vector3D unit = new Vector3D(0, 1, 0);
		double hr = Math.toRadians(heading);
		Rotation rotation = new Rotation(RotationOrder.XYZ, 0.0, 0.0, hr + Math.toRadians(angle));
		unit = rotation.applyTo(unit);
		Vector3D location = new Vector3D(x, y, 0);
		// step size half the world block size
		int inc = Math.max(1, currentWorld.getBlockSize() / 4);

		double bestMatchDistance = maxDistance;
		double bestMatchOccupancy = 0;

		for (int i = 0; i < maxDistance; i += 1)
		{
			double d = currentWorld.get(location.getX(), location.getY());
			if (d >= occupancyThreshold && d > bestMatchOccupancy)
			{
				bestMatchDistance = i;
				bestMatchOccupancy = d;

			}
			location = location.add(unit);
		}
		return bestMatchDistance;
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

	public double getScanMatchRating()
	{
		return totalVotes / totalObservations;
	}

	public int getSampleCount()
	{
		return (int) totalObservations;

	}

	public void addScanReference(final ParticleFilterObservationSet observations)
	{
		Pose pose = new Pose(x, y, heading);
		scanReferences.add(new ScanReference()
		{

			@Override
			public Pose getScanOrigin()
			{
				return pose;
			}

			@Override
			public ParticleFilterObservationSet getScan()
			{
				return observations;
			}
		});

	}

}
