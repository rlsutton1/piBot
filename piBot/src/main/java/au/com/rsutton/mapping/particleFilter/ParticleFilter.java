package au.com.rsutton.mapping.particleFilter;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.mapping.probability.ProbabilityMap;

public class ParticleFilter
{

	List<Particle> particles = new LinkedList<>();
	final int particleQty;

	ParticleFilter(ProbabilityMap map, int particles)
	{
		particleQty = particles;
		createRandomStart(map);
	}

	private void createRandomStart(ProbabilityMap map)
	{
		int maxX = map.getMaxX();
		int minX = map.getMinX();
		int maxY = map.getMaxY();
		int minY = map.getMinY();

		int xd = maxX - minX;
		int yd = maxY - minY;

		particles.clear();
		// generate initial random scattering of particles
		Random rand = new Random();

		for (int i = 0; i < particleQty; i++)
		{
			double x = (int) ((rand.nextDouble() * xd) + minX);
			double y = (int) ((rand.nextDouble() * yd) + minY);
			double heading = (int) (rand.nextDouble() * 360);
			particles.add(new Particle(x, y, heading));
		}
	}

	public void moveParticles(ParticleUpdate update)
	{
		for (Particle particle : particles)
		{
			particle.move(update);
		}
	}

	public void addObservation(ProbabilityMap currentWorld, RobotLocation observations)
	{
		for (Particle particle : particles)
		{
			particle.addObservation(currentWorld, observations);
		}
	}

	public void resample(ProbabilityMap map)
	{
		Random rand = new Random();
		List<Particle> newSet = new LinkedList<>();
		int pos = 0;
		double next = 0.0;
		Particle selectedParticle = null;
		double bestRating = 0;
		while (newSet.size() < particleQty)
		{
			next += rand.nextDouble() * 2.0;// 50/50 chance of the same or next
											// being picked if there are both
											// rated 1.0
			while (next > 0.0)
			{
				selectedParticle = particles.get(pos);

				next -= selectedParticle.getRating();
				pos++;
				pos %= particleQty;

			}
			// System.out.println(selectedParticle.getRating());
			bestRating = Math.max(bestRating, selectedParticle.getRating());
			newSet.add(new Particle(selectedParticle.getX(), selectedParticle.getY(), selectedParticle.getHeading()));
		}
		System.out.println("Best rating " + bestRating);
		if (bestRating < 0.02 && !getStdDev())
		{
			createRandomStart(map);
		} else
		{
			particles = newSet;
		}
	}

	public void dumpTextWorld(ProbabilityMap map)
	{
		for (Particle particle : particles)
		{
			map.updatePoint((int) particle.getX(), (int) particle.getY(), 1.0, 10);
		}

		map.dumpTextWorld();
	}

	public Vector3D dumpAveragePosition()
	{
		double x = 0;
		double y = 0;

		double h = 0;
		double h1c = 0;
		double h2c = 0;
		double h1 = 0;
		double h2 = 0;
		for (Particle particle : particles)
		{
			x += particle.getX();
			y += particle.getY();
			double heading = particle.getHeading();
			if (heading > 180)
			{
				h2c++;
				h2 += heading;
			} else
			{
				h1c++;
				h1 += heading;
			}

		}
		h1 = h1 / h1c;
		h2 = h2 / h2c;
		if (h1c < 1)
		{
			h1 = h2;
			h1c = h2c;
		}
		if (h2c < 1)
		{
			h2 = h1;
			h2c = h1c;
		}

		Vector3D unit = new Vector3D(0, 1, 0);

		Rotation r1 = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(h1));

		Rotation r2 = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(h2));

		Vector3D v1 = r1.applyTo(unit).scalarMultiply(h1c / h2c);
		Vector3D v2 = r2.applyTo(unit).scalarMultiply(h2c / h1c);

		h = Math.toDegrees(Vector3D.angle(unit, v1.add(v2)));
		
		System.out.println("Average Heading "+h);

		return new Vector3D((x / particles.size()) , (y / particles.size()),0);

	}

	

	int counter = 10;
	public boolean getStdDev()
	{
		
		StandardDeviation	xdev = new StandardDeviation();
		StandardDeviation	ydev = new StandardDeviation();
		StandardDeviation	headingDev = new StandardDeviation();
		
		
		for (Particle particle:particles)
		{
			xdev.increment(particle.x);
			ydev.increment(particle.y);
			//xdev.increment(particle.x);
		}
		double dev = xdev.getResult()+ydev.getResult();
		System.out.println("Deviation "+dev);
		return dev < 40;
	}

}
