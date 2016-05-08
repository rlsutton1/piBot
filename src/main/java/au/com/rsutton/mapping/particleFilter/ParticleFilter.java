package au.com.rsutton.mapping.particleFilter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.ui.MapDataSource;
import au.com.rsutton.ui.PointSource;

public class ParticleFilter
{

	List<Particle> particles = new LinkedList<>();
	final int particleQty;
	private double averageHeading;

	volatile double bestRating = 0;

	
	AtomicReference<ParticleFilterObservationSet> lastObservation = new AtomicReference<>();

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

	public void addObservation(ProbabilityMap currentWorld, ParticleFilterObservationSet observations, double compassAdjustment)
	{
		lastObservation.set(observations);
		
		particles.parallelStream().forEach(e->e.addObservation(currentWorld,observations,compassAdjustment));
//		for (Particle particle : particles)
//		{
//			particle.addObservation(currentWorld, observations,compassAdjustment);
//		}
	}

	public void resample(ProbabilityMap map)
	{
		Random rand = new Random();
		List<Particle> newSet = new LinkedList<>();
		int pos = 0;
		double bestRatingSoFar = 0;
		double next = 0.0;
		Particle selectedParticle = null;
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
			bestRatingSoFar = Math.max(bestRatingSoFar, selectedParticle.getRating());
			newSet.add(new Particle(selectedParticle.getX(), selectedParticle.getY(), selectedParticle.getHeading()));
		}
		bestRating = bestRatingSoFar;
		System.out.println("Best rating " + bestRating);
		if (bestRating < 0.02 && getStdDev() > 30)
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
		
//		TopNList<Particle>  top = new TopNList<Particle>(550);
//		for (Particle particle:particles)
//		{
//			top.add(particle.getRating(), particle);
//		}
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

		h = Math.toDegrees(Math.atan2(v1.add(v2).getY(), v1.add(v2).getX())) - 90;

		if (h < 0)
		{
			h += 360;
		}

		System.out.println("Average Heading " + h);

		averageHeading = h;

		return new Vector3D((x / particles.size()), (y / particles.size()), 0);

	}

	public double getAverageHeading()
	{
		return averageHeading;
	}

	int counter = 10;

	public double getStdDev()
	{

		StandardDeviation xdev = new StandardDeviation();
		StandardDeviation ydev = new StandardDeviation();
		StandardDeviation headingDev = new StandardDeviation();

		for (Particle particle : particles)
		{
			xdev.increment(particle.x);
			ydev.increment(particle.y);
			// xdev.increment(particle.x);
		}
		double dev = xdev.getResult() + ydev.getResult();
		System.out.println("Deviation " + dev);
		return dev;
	}

	PointSource getParticlePointSource()
	{
		return new PointSource()
		{

			@Override
			public List<Point> getOccupiedPoints()
			{
				TopNList<Point>  top = new TopNList<Point>(40);
				
				for (Particle particle : particles)
				{
					top.add(particle.getRating(), new Point((int) particle.getX(), (int) particle.getY()));
					
				}
				return top.getTop();
			}
		};
	}

	MapDataSource getHeadingMapDataSource()
	{
		return new MapDataSource()
		{

			@Override
			public List<Point> getPoints()
			{
				Vector3D pos = dumpAveragePosition();
				List<Point> points = new LinkedList<>();
				points.add(new Point((int) pos.getX(), (int) pos.getY()));
				return points;
			}

			@Override
			public void drawPoint(BufferedImage image, double pointOriginX, double pointOriginY, double scale)
			{
				Graphics graphics = image.getGraphics();

				// draw robot body
				graphics.setColor(new Color(0, 128, 128));
				int robotSize = 30;
				graphics.drawOval((int) (pointOriginX - (robotSize * 0.5 * scale)),
						(int) (pointOriginY - (robotSize * 0.5 * scale)), (int) (robotSize * scale),
						(int) (robotSize * scale));


				if (lastObservation.get() != null)
				{
					graphics.setColor(new Color(0, 0, 255));
					// draw lidar observation lines
					for (ScanObservation obs : lastObservation.get().getObservations())
					{
						Vector3D vector = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(averageHeading))
								.applyTo(obs.getVector());
						graphics.drawLine((int) pointOriginX, (int) pointOriginY,
								(int) (pointOriginX + (vector.getX() * scale)),
								(int) (pointOriginY + (vector.getY() * scale)));
					}
				}
				// draw heading line
				graphics.setColor(new Color(0, 128, 128));
				Vector3D line = new Vector3D(60 * scale, 0, 0);
				line = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(averageHeading + 90)).applyTo(line);

				graphics.drawLine((int) pointOriginX, (int) pointOriginY, (int) (pointOriginX + line.getX()),
						(int) (pointOriginY + line.getY()));

				
			}
		};
	}

	public int getSampleCount()
	{
		return particles.get(0).getSampleCount();
	}

	public Double getBestRating()
	{
		return bestRating;
	}

}
