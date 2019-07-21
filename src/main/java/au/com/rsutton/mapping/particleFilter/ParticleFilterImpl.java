package au.com.rsutton.mapping.particleFilter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Stopwatch;

import au.com.rsutton.angle.AngleUtil;
import au.com.rsutton.angle.WeightedAngle;
import au.com.rsutton.hazelcast.DataLogLevel;
import au.com.rsutton.hazelcast.DataLogValue;
import au.com.rsutton.hazelcast.LidarScan;
import au.com.rsutton.mapping.array.Dynamic2dSparseArrayFactory;
import au.com.rsutton.mapping.array.SparseArray;
import au.com.rsutton.mapping.probability.Occupancy;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.navigation.feature.RobotLocationDeltaListener;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.robot.lidar.LidarObservation;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.ui.DataSourcePoint;
import au.com.rsutton.ui.DataSourceStatistic;
import au.com.rsutton.ui.MapDrawingWindow;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.AngleUnits;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;

public class ParticleFilterImpl implements ParticleFilterIfc
{

	private static final double MINIMUM_MEANINGFUL_RATING = 0.0001;
	private final List<Particle> particles = new CopyOnWriteArrayList<>();
	private volatile int particleQty;
	private volatile double averageHeading;

	private volatile double bestScanMatchScore = 0;

	private final AtomicReference<LidarScan> lastObservation = new AtomicReference<>();
	private final double headingNoise;
	private final double distanceNoise;
	private double stablisedHeading = 0;
	private ParticleFilterListener listener;

	private final DistanceUnit distanceUnit = DistanceUnit.CM;

	volatile private double bestRawScore;

	private ProbabilityMapIIFc map;
	private final RobotLocationDeltaListener observer;
	private final MapDrawingWindow ui;

	private final Logger logger = LogManager.getLogger();

	public ParticleFilterImpl(ProbabilityMapIIFc map, int particles, double distanceNoise, double headingNoise,
			StartPosition startPosition, RobotInterface robot, Pose pose)
	{
		this.headingNoise = headingNoise;
		this.distanceNoise = distanceNoise;
		this.map = buildMatchingMap(map);
		particleQty = particles;
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

		ui = new MapDrawingWindow("Particle Filter", 1200, 0, 250, true);
		addDataSoures(ui);
		ui.addDataSource(map, new Color(255, 255, 255));
	}

	@Override
	public void updateMap(ProbabilityMapIIFc map)
	{
		this.map = buildMatchingMap(map);
	}

	public static ProbabilityMap buildMatchingMap(ProbabilityMapIIFc source)
	{
		ProbabilityMap matchMap = new ProbabilityMap(5);
		matchMap.setDefaultValue(0.0);
		matchMap.erase();
		int radius = 25;

		int minX = source.getMinX();
		int maxX = source.getMaxX();
		int minY = source.getMinY();
		int maxY = source.getMaxY();

		for (int x = minX; x < maxX + 1; x++)
		{
			for (int y = minY; y < maxY + 1; y++)
			{
				double value = source.get(x, y);

				if (value > 0.5)
				{
					matchMap.updatePoint(x, y, Occupancy.OCCUPIED, 1, radius);
				}

			}
		}

		return matchMap;
	}

	private final ReentrantLock lock = new ReentrantLock();

	private RobotLocationDeltaListener getObserver()
	{
		return new RobotLocationDeltaListener()
		{

			@Override
			public void onMessage(final Angle deltaHeading, final Distance deltaDistance, boolean bump)
			{

				if (lock.tryLock())
				{

					try
					{

						moveParticles(new ParticleUpdate()
						{

							@Override
							public double getDeltaHeading()
							{

								double degrees = deltaHeading.getDegrees();
								new DataLogValue("Move Particles DH(1)", "" + degrees, DataLogLevel.INFO).publish();
								if (degrees > 180)
								{
									degrees = 360 - degrees;

								} else
								{
									degrees = degrees * -1.0;
								}

								new DataLogValue("Move Particles DH", "" + degrees, DataLogLevel.INFO).publish();

								return degrees;
							}

							@Override
							public double getMoveDistance()
							{
								return deltaDistance.convert(DistanceUnit.CM);
							}
						});

					} finally
					{
						lock.unlock();
					}

				}
			}

			@Override
			public void onMessage(LidarScan scan)
			{
				if (lock.tryLock())
				{
					try
					{
						addObservation(downSampleObservations(scan));
					} finally
					{
						lock.unlock();
					}
				}
			}

		};
	}

	private void createFixedStart(int x, int y, int heading)
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

	private void addObservation(LidarScan lidarScan)
	{

		lastObservation.set(lidarScan);

		double stdDev = getStdDev();
		boolean isLost = stdDev > 100;

		particles.parallelStream().forEach(e -> {
			e.addObservation(map, lidarScan, isLost);
		});

		// adjust the number of particles in the particle filter based on
		// how well localised it is 500 < X < 5000
		int newParticleCount = Math.max(500, Math.min(5000, (int) (20 * stdDev)));

		resample(newParticleCount);

	}

	/**
	 * Down sample the observations by adding them to an occupancy grid, then
	 * rebuilding the scans from the occupancy grid, thus eliminating scans that
	 * are very close together - this is a problem that occurs when approaching
	 * walls, the nearest wall becomes over represented to the particle filter.
	 * 
	 * @param lidarScan
	 * @return
	 */
	private LidarScan downSampleObservations(LidarScan lidarScan)
	{
		List<LidarObservation> result = new LinkedList<>();

		int resolution = 5;

		SparseArray<Double> array = Dynamic2dSparseArrayFactory.getDynamic2dSparseArray(0.0);
		for (ScanObservation obs : lidarScan.getObservations())
		{
			// 5cm resolution
			int x = obs.getX() / resolution;
			int y = obs.getY() / resolution;
			array.set(x, y, 1.0);
		}

		int maxX = array.getMaxX();
		int maxY = array.getMaxY();
		for (int x = array.getMinX(); x <= maxX; x++)
		{
			for (int y = array.getMinY(); y <= maxY; y++)
			{
				if (array.get(x, y) > 0.9)
				{
					result.add(new LidarObservation(new Vector3D(x * resolution, y * resolution, 0)));
				}

			}
		}
		LidarScan augmentedScan = new LidarScan(lidarScan);
		augmentedScan.setObservations(result);

		return augmentedScan;
	}

	private void moveParticles(ParticleUpdate update)
	{
		logger.debug("Delta heading " + update.getDeltaHeading() + " Delta move " + update.getMoveDistance());
		if (update.getDeltaHeading() > 180 || update.getDeltaHeading() < -180)
		{
			logger.error("What hte this is crazy");
		}
		for (Particle particle : particles)
		{
			particle.move(update);
		}

		// stablisedHeading += update.getDeltaHeading();
		// stablisedHeading = HeadingHelper.normalizeHeading(stablisedHeading);
	}

	private int poorMatches = 0;

	private void resample(int newParticleCount)
	{

		Stopwatch timer = Stopwatch.createStarted();

		Random rand = new Random();
		List<Particle> newSet = new LinkedList<>();

		double totalRating = 0;
		double maxRating = 0;
		double bestRawSoFar = 1000000;
		for (Particle selectedParticle : particles)
		{
			bestRawSoFar = Math.min(bestRawSoFar, selectedParticle.getRating());
			totalRating += selectedParticle.getRating();
			maxRating = Math.max(maxRating, selectedParticle.getRating());
		}

		new DataLogValue("PF best:", "" + maxRating, DataLogLevel.INFO).publish();

		// 90 ok
		if (maxRating < 0.80)
		{
			poorMatches++;
			if (poorMatches > 5 && particleFilterStatus == ParticleFilterStatus.LOCALIZED)
			{
				particleFilterStatus = ParticleFilterStatus.POOR_MATCH;
			}
		} else
		{
			poorMatches = Math.max(0, poorMatches - 1);
		}
		poorMatches = Math.min(poorMatches, 5);
		if (poorMatches == 0 && particleFilterStatus == ParticleFilterStatus.POOR_MATCH)
		{
			particleFilterStatus = ParticleFilterStatus.LOCALIZED;
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
			selectNewParticlesV2(newParticleCount, rand, newSet);
		}
		bestScanMatchScore = bestRatingSoFar;
		bestRawScore = bestRawSoFar;
		logger.debug("Best rating " + bestScanMatchScore);
		if (bestScanMatchScore < MINIMUM_MEANINGFUL_RATING && getStdDev() > 60)
		{
			// there is no useful data, re-seed the particle filter
			// createRandomStart();
		} else
		{
			particles.clear();
			particles.addAll(newSet);
		}
		particleQty = newParticleCount;

		new DataLogValue("PF-particle count", "" + particleQty, DataLogLevel.INFO).publish();

		logger.debug("Resample took " + timer.elapsed(TimeUnit.MILLISECONDS));

		if (listener != null)
		{
			listener.update(getXyPosition(), new Angle(stablisedHeading, AngleUnits.DEGREES), getStdDev(),
					lastObservation.get(), particleFilterStatus);
		}

	}

	class ParticleRef implements Comparable<Double>
	{
		Particle particle;
		Double value;

		ParticleRef(Particle particle, double value)
		{
			this.particle = particle;
			this.value = value;
		}

		@Override
		public int compareTo(Double arg0)
		{
			return value.compareTo(arg0);
		}
	}

	private void selectNewParticlesV2(int newParticleCount, Random rand, List<Particle> newSet)
	{

		List<ParticleRef> working = new ArrayList<>(particles.size());

		double pos = 0;
		for (Particle particle : particles)
		{
			working.add(new ParticleRef(particle, pos));
			pos += particle.getRescaledRating();
		}

		Particle selectedParticle = null;
		for (int i = 0; i < newParticleCount; i++)
		{
			double key = rand.nextDouble() * pos;
			int rawIdx = Collections.binarySearch(working, key);
			int idx = rawIdx;

			if (idx < 0)
			{
				idx = Math.abs(idx) - 2;
			} else
			{
				idx = idx - 1;
			}
			selectedParticle = working.get(idx).particle;
			newSet.add(new Particle(selectedParticle));
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
			String message = "removing " + particlesToRemove.size() + " useless particles";
			logger.debug(message);
			new DataLogValue("PF-useless particles", "" + particlesToRemove.size(), DataLogLevel.INFO).publish();
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

	private DistanceXY getXyPosition()
	{
		double x = 0;
		double y = 0;

		List<WeightedAngle> angles = new LinkedList<>();

		for (Particle particle : particles)
		{
			x += particle.getX();
			y += particle.getY();
			angles.add(new WeightedAngle(particle.getHeading(), 1));

		}

		// System.out.println("Average Heading " + h);

		averageHeading = AngleUtil.getAverageAngle(angles);

		stablisedHeading = averageHeading;
		// stablisedHeading = stablisedHeading
		// + (HeadingHelper.getChangeInHeading(averageHeading, stablisedHeading)
		// * 0.1);

		return new DistanceXY((x / particles.size()), (y / particles.size()), DistanceUnit.CM);

	}

	private ParticleFilterStatus particleFilterStatus = ParticleFilterStatus.LOCALIZING;

	/*
	 * (non-Javadoc)
	 * 
	 * @see au.com.rsutton.mapping.particleFilter.ParticleFilterIfc#getStdDev()
	 */
	private double getStdDev()
	{

		StandardDeviation xdev = new StandardDeviation();
		StandardDeviation ydev = new StandardDeviation();

		for (Particle particle : particles)
		{
			xdev.increment(particle.x);
			ydev.increment(particle.y);
		}
		double dev = xdev.getResult() + ydev.getResult();
		// System.out.println("Deviation " + dev);

		if (dev < 30 && particleFilterStatus == ParticleFilterStatus.LOCALIZING)
		{
			particleFilterStatus = ParticleFilterStatus.LOCALIZED;
		}

		new DataLogValue("ParticleFilterStatus", "" + particleFilterStatus, particleFilterStatus.getDataLogLevel())
				.publish();

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
				DistanceXY pos = getXyPosition();
				List<Point> points = new LinkedList<>();
				points.add(new Point((int) pos.getX().convert(distanceUnit), (int) pos.getY().convert(distanceUnit)));
				return points;
			}

			@Override
			public void drawPoint(BufferedImage image, double pointOriginX, double pointOriginY, double scale,
					double originalX, double originalY)
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

						double xend = pointOriginX + (vector.getX() * scale);
						double yend = pointOriginY + (vector.getY() * scale);
						graphics.drawLine((int) pointOriginX, (int) pointOriginY, (int) xend, (int) yend);
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

	private void addDataSoures(MapDrawingWindow ui)
	{
		ui.addDataSource(getHeadingMapDataSource());
		ui.addDataSource(getParticlePointSource(), new Color(255, 0, 0));

		ui.addStatisticSource(new DataSourceStatistic()
		{

			@Override
			public String getValue()
			{
				return "" + bestScanMatchScore + " " + bestRawScore;
			}

			@Override
			public String getLabel()
			{
				return "Best Match";
			}
		});

		ui.addStatisticSource(new DataSourceStatistic()
		{

			@Override
			public String getValue()
			{

				return "" + getStdDev();

			}

			@Override
			public String getLabel()
			{
				return "StdDev";
			}
		});

	}

	@Override
	public void removeListener(ParticleFilterListener listener)
	{
		listener = null;

	}

}
