package au.com.rsutton.mapping.particleFilter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import com.google.common.base.Stopwatch;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.mapping.probability.Occupancy;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.robot.RobotListener;
import au.com.rsutton.robot.rover.Angle;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.ui.DataSourcePoint;

public class ParticleFilterImpl implements ParticleFilterIfc
{

	private static final double MINIMUM_MEANINGFUL_RATING = 0.001;
	private final List<Particle> particles = new CopyOnWriteArrayList<>();
	private volatile int particleQty;
	private volatile double averageHeading;

	private volatile double bestScanMatchScore = 0;

	private AtomicReference<ParticleFilterObservationSet> lastObservation = new AtomicReference<>();
	private final double headingNoise;
	private final double distanceNoise;
	private double stablisedHeading = 0;
	private ParticleFilterListener listener;
	private int lastSignum;
	private double lastObservationAngle;

	Stopwatch lastResample = Stopwatch.createStarted();
	volatile private double bestRawScore;

	private boolean stop = false;
	private RobotInterface robot;
	private ProbabilityMapIIFc map;
	private RobotListener observer;

	public ParticleFilterImpl(ProbabilityMapIIFc map, int particles, double distanceNoise, double headingNoise,
			StartPosition startPosition, RobotInterface robot, Pose pose)
	{
		this.headingNoise = headingNoise;
		this.distanceNoise = distanceNoise;
		this.robot = robot;
		this.map = map;
		particleQty = particles;
		newParticleCount = particleQty;
		if (startPosition == StartPosition.RANDOM)
		{
			createRandomStart();
		} else if (startPosition == StartPosition.USE_POSE)
		{
			createFixedStart((int) pose.getX(), (int) pose.getY(), (int) pose.getHeading());
		} else
		{
			createFixedStart(0, 0, 0);
		}

		observer = getObserver();

		robot.addMessageListener(observer);
	}

	private RobotListener getObserver()
	{
		return new RobotListener()
		{

			private Double lasty;
			private Double lastx;
			private Angle lastheading;

			@Override
			public void observed(RobotLocation robotLocation)
			{

				addObservation(robotLocation);

				if (lastx != null)
				{
					moveParticles(new ParticleUpdate()
					{

						@Override
						public double getDeltaHeading()
						{

							return HeadingHelper.getChangeInHeading(
									robotLocation.getDeadReaconingHeading().getDegrees(), lastheading.getDegrees());
						}

						@Override
						public double getMoveDistance()
						{
							double dx = (lastx - robotLocation.getX().convert(DistanceUnit.CM));
							double dy = (lasty - robotLocation.getY().convert(DistanceUnit.CM));
							return Vector3D.distance(Vector3D.ZERO, new Vector3D(dx, dy, 0));
						}
					});
				}
				lasty = robotLocation.getY().convert(DistanceUnit.CM);
				lastx = robotLocation.getX().convert(DistanceUnit.CM);
				lastheading = robotLocation.getDeadReaconingHeading();

			}
		};
	}

	void createFixedStart(int x, int y, int heading)
	{
		particles.clear();
		// generate initial random scattering of particles

		for (int i = 0; i < particleQty; i++)
		{
			particles.add(new Particle(x, y, heading, distanceNoise, headingNoise));
		}
	}

	private void createRandomStart()
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
			particles.add(new Particle(x, y, heading, distanceNoise, headingNoise));
		}
	}

	public synchronized void addObservation(ParticleFilterObservationSet observations)
	{

		if (stop)
		{
			return;
		}
		lastObservation.set(observations);

		particles.parallelStream().forEach(e -> {
			boolean isLost = getStdDev() > 100;
			e.addObservation(map, observations, isLost);
		});

		boolean resampleRequired = false;
		if (!observations.getObservations().isEmpty())
		{
			double currentObservationAngle = observations.getObservations().iterator().next().getAngleRadians();

			int signum = (int) Math.signum(currentObservationAngle - lastObservationAngle);

			lastObservationAngle = currentObservationAngle;

			if (signum != lastSignum || lastResample.elapsed(TimeUnit.MILLISECONDS) > 1500)
			{
				resampleRequired = true;
			}
			lastSignum = signum;
		}
		if (resampleRequired)
		{
			if (lastResample.elapsed(TimeUnit.MILLISECONDS) > 1500)
			{
				System.out.println("************************* resample over due by "
						+ (lastResample.elapsed(TimeUnit.MILLISECONDS) - 1500)
						+ " *******************************************");
			}
			lastResample.reset();
			lastResample.start();
			resample();
		}
	}

	public void moveParticles(ParticleUpdate update)
	{
		System.out.println("Delta move " + update.getDeltaHeading() + " " + update.getMoveDistance());
		for (Particle particle : particles)
		{
			particle.move(update);
		}

		stablisedHeading += update.getDeltaHeading();
	}

	protected synchronized void resample()
	{

		Stopwatch timer = Stopwatch.createStarted();

		Random rand = new Random();
		List<Particle> newSet = new LinkedList<>();
		int pos = 0;
		double next = 0.0;
		double totalRating = 0;
		double maxRating = 0;
		double bestRawSoFar = 1000000;
		for (Particle selectedParticle : particles)
		{
			bestRawSoFar = Math.min(bestRawSoFar, selectedParticle.getRating());
			totalRating += selectedParticle.getRating();
			maxRating = Math.max(maxRating, selectedParticle.getRating());
		}

		for (Particle selectedParticle : particles)
		{
			selectedParticle.setRescaledRating(selectedParticle.getRating() / totalRating);
		}

		removeUnusableParticles();

		double bestRatingSoFar = 0;
		for (Particle selectedParticle : particles)
		{
			bestRatingSoFar = Math.max(bestRatingSoFar, selectedParticle.getRescaledRating());
		}
		if (bestRatingSoFar < MINIMUM_MEANINGFUL_RATING)
		{
			for (Particle selectedParticle : particles)
			{
				newSet.add(new Particle(selectedParticle));

			}
		} else
		{
			Particle selectedParticle = null;

			List<Particle> working = new LinkedList<>();
			working.addAll(particles);

			while (!working.isEmpty() && newSet.size() < newParticleCount)
			{
				next += rand.nextDouble() * 2.0;// 50/50 chance of the same or
												// next
												// being picked if they are
												// both
												// rated 1.0
				while (next > 0.0)
				{
					selectedParticle = working.get(pos);

					next -= selectedParticle.getRescaledRating();
					pos++;
					pos %= working.size();

				}
				// System.out.println(selectedParticle.getRating());
				newSet.add(new Particle(selectedParticle));
			}
		}
		bestScanMatchScore = bestRatingSoFar;
		bestRawScore = bestRawSoFar;
		System.out.println("Best rating " + bestScanMatchScore);
		if (bestScanMatchScore < MINIMUM_MEANINGFUL_RATING && getStdDev() > 60)
		{
			// there is no useful data, re-seed the particle filter
			createRandomStart();
		} else
		{
			particles.clear();
			particles.addAll(newSet);
		}
		particleQty = newParticleCount;

		System.out.println("Resample took " + timer.elapsed(TimeUnit.MILLISECONDS));

		if (listener != null)
		{
			listener.update(dumpAveragePosition(), stablisedHeading, getStdDev(), lastObservation.get());
		}

	}

	private void removeUnusableParticles()
	{
		List<Particle> particlesToRemove = new LinkedList<>();
		for (Particle particle : particles)
		{
			if (particle.getRescaledRating() <= 1.0 / particles.size())
			{
				particlesToRemove.add(particle);
			}
		}

		// always keep at least 100 particles, if there are less than 100 good
		// particles then we probably should keep all our particles
		if (particlesToRemove.size() + 100 < particles.size())
		{
			System.out.println("removing " + particlesToRemove.size() + " useless particles");
			// strip particles with ratings that are below the minimum threshold
			particles.removeAll(particlesToRemove);

		}

	}

	public void dumpTextWorld()
	{
		for (Particle particle : particles)
		{
			map.updatePoint((int) particle.getX(), (int) particle.getY(), Occupancy.OCCUPIED, 1.0, map.getBlockSize());
		}

		map.dumpTextWorld();
	}

	@Override
	public Vector3D dumpAveragePosition()
	{
		double x = 0;
		double y = 0;

		double h = 0;
		double h1c = 0;
		double h2c = 0;
		double h1 = 0;
		double h2 = 0;

		// TopNList<Particle> top = new TopNList<Particle>(550);
		// for (Particle particle:particles)
		// {
		// top.add(particle.getRating(), particle);
		// }
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

		// System.out.println("Average Heading " + h);

		averageHeading = h;

		stablisedHeading = stablisedHeading
				+ (HeadingHelper.getChangeInHeading(averageHeading, stablisedHeading) * 0.5);

		return new Vector3D((x / particles.size()), (y / particles.size()), 0);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * au.com.rsutton.mapping.particleFilter.ParticleFilterIfc#getAverageHeading
	 * ()
	 */
	@Override
	public double getAverageHeading()
	{
		return averageHeading;
	}

	int counter = 10;
	private int newParticleCount;

	/*
	 * (non-Javadoc)
	 * 
	 * @see au.com.rsutton.mapping.particleFilter.ParticleFilterIfc#getStdDev()
	 */
	@Override
	public double getStdDev()
	{

		StandardDeviation xdev = new StandardDeviation();
		StandardDeviation ydev = new StandardDeviation();
		StandardDeviation headingDev = new StandardDeviation();

		for (Particle particle : particles)
		{
			xdev.increment(particle.x);
			ydev.increment(particle.y);
		}
		double dev = xdev.getResult() + ydev.getResult();
		// System.out.println("Deviation " + dev);
		return dev;
	}

	@Override
	public DataSourcePoint getParticlePointSource()
	{
		return new DataSourcePoint()
		{

			@Override
			public List<Point> getOccupiedPoints()
			{
				List<Point> points = new LinkedList<>();

				for (Particle particle : particles)
				{
					points.add(new Point((int) particle.getX(), (int) particle.getY()));

				}
				return points;
			}
		};
	}

	/**
	 * draw recent observations
	 * 
	 * @return
	 */
	@Override
	public DataSourceMap getHeadingMapDataSource()
	{
		return new DataSourceMap()
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
						Vector3D vector = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(stablisedHeading))
								.applyTo(obs.getVector());
						graphics.drawLine((int) pointOriginX, (int) pointOriginY,
								(int) (pointOriginX + (vector.getX() * scale)),
								(int) (pointOriginY + (vector.getY() * scale)));
					}
				}
				// draw heading line
				graphics.setColor(new Color(0, 128, 128));
				Vector3D line = new Vector3D(60 * scale, 0, 0);
				line = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(stablisedHeading + 90)).applyTo(line);

				graphics.drawLine((int) pointOriginX, (int) pointOriginY, (int) (pointOriginX + line.getX()),
						(int) (pointOriginY + line.getY()));

			}
		};
	}

	@Override
	public Double getBestScanMatchScore()
	{
		return bestScanMatchScore;
	}

	@Override
	public void setParticleCount(int i)
	{
		newParticleCount = i;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * au.com.rsutton.mapping.particleFilter.ParticleFilterIfc#addListener(au
	 * .com.rsutton.mapping.particleFilter.ParticleFilterListener)
	 */
	@Override
	public void addListener(ParticleFilterListener listener)
	{
		this.listener = listener;

	}

	@Override
	public void addPendingScan(ParticleFilterObservationSet par)
	{
		for (Particle particle : particles)
		{
			particle.addScanReference(par);
		}

	}

	@Override
	public List<Particle> getParticles()
	{

		return new LinkedList<>(particles);
	}

	@Override
	public Double getBestRawScore()
	{
		return bestRawScore;
	}

	@Override
	public void shutdown()
	{
		stop = true;
		robot.removeMessageListener(observer);

	}
}
