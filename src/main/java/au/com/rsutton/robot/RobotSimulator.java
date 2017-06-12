package au.com.rsutton.robot;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.mapping.particleFilter.Particle;
import au.com.rsutton.mapping.particleFilter.ScanObservation;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.navigation.feature.RobotLocationDeltaListener;
import au.com.rsutton.robot.rover.Angle;
import au.com.rsutton.robot.rover.AngleUnits;
import au.com.rsutton.robot.rover.LidarObservation;
import au.com.rsutton.ui.DataSourceMap;

public class RobotSimulator implements DataSourceMap, RobotInterface, Runnable
{

	public static final double REQUIRED_POINT_CERTAINTY = 0.75;

	Logger logger = LogManager.getLogger();

	Random random = new Random();
	private ProbabilityMapIIFc map;

	double x = 0;
	double y = 0;

	double totalDistanceTravelled = 0;

	volatile double headingOffset = random.nextInt(360);

	double heading;
	private volatile boolean freeze;
	private double rspeed;
	private List<RobotLocationDeltaListener> listeners = new CopyOnWriteArrayList<>();
	boolean freezeSet = false;

	private double requestedDeltaHeading;

	public RobotSimulator(ProbabilityMapIIFc map)
	{
		this.map = map;

		new Thread(this, "Robot").start();
	}

	public double move(double distance)
	{
		if (freeze)
		{
			return 0;
		}
		if (Math.abs(distance - 0.0) > 0.2)
		{
			distance -= Math.abs(distance * (random.nextGaussian() * 0.5));
		}

		Vector3D unit = new Vector3D(0, distance, 0);
		Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(heading));
		Vector3D location = new Vector3D(x, y, 0);
		Vector3D newLocation = location.add(rotation.applyTo(unit));

		double nx = newLocation.getX();
		double ny = newLocation.getY();
		// never drive through a wall
		if (map.get(nx, ny) <= 0.5)
		{
			x = nx;
			y = ny;
			totalDistanceTravelled += distance;

			return distance;
		} else
		{
			logger.info("Avoiding wall");
			return 0;
		}

	}

	public double internalTurn(double angle)
	{
		if (freeze)
		{
			return 0;
		}
		double noise = Math.abs((random.nextGaussian() * 1.5) * (1.0 / hz));
		double delta = angle + noise;
		heading += delta;
		if (heading < 0)
		{
			heading += 360.0;
		}
		if (heading > 360)
		{
			heading -= 360.0;
		}
		return delta;

	}

	/**
	 * simulates a full scan per second
	 * 
	 * @param msSinceLastScan
	 * @return
	 */
	public List<ScanObservation> getObservation(double fromPercentage, double toPercentage)
	{

		List<ScanObservation> observations = new LinkedList<>();

		Particle particle = new Particle(x, y, heading, 2, 2);

		Random rand = new Random();
		double stepSize = 2.4;
		double stepNoise = 1.2;

		int from = (int) (fromPercentage * (360.0 / 100.0));
		int to = (int) (toPercentage * (360.0 / 100.0));
		for (double h = from; h < to; h += stepSize + Math.abs((rand.nextGaussian() * stepNoise)))
		{
			double adjustedHeading = h - 180 + 45;
			double distance = particle.simulateObservation(map, adjustedHeading, 1000, REQUIRED_POINT_CERTAINTY);

			if (Math.abs(distance) > 1 && Math.abs(distance) < 1000)
			{
				Vector3D unit = new Vector3D(0, 1, 0).scalarMultiply(distance);
				Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(adjustedHeading));
				Vector3D distanceVector = rotation.applyTo(unit);
				observations.add(new LidarObservation(distanceVector, true));
			}
		}

		if (observations.isEmpty())
		{
			logger.info("NO observations! " + from + " " + to);
		}

		return observations;
	}

	public void setLocation(int x, int y, int heading)
	{
		this.x = x;
		this.y = y;
		this.heading = heading;

	}

	@Override
	public List<Point> getPoints()
	{
		List<Point> points = new LinkedList<>();
		points.add(new Point((int) x, (int) y));
		return points;
	}

	@Override
	public void drawPoint(BufferedImage image, double pointOriginX, double pointOriginY, double scale)
	{
		Graphics graphics = image.getGraphics();

		graphics.setColor(new Color(0, 255, 0));
		int robotSize = 30;
		graphics.drawOval((int) (pointOriginX - (robotSize * 0.5 * scale)),
				(int) (pointOriginY - (robotSize * 0.5 * scale)), (int) (robotSize * scale), (int) (robotSize * scale));

		Vector3D line = new Vector3D(60 * scale, 0, 0);
		line = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(heading + 90)).applyTo(line);

		graphics.drawLine((int) pointOriginX, (int) pointOriginY, (int) (pointOriginX + line.getX()),
				(int) (pointOriginY + line.getY()));

	}

	@Override
	public void freeze(boolean b)
	{
		freeze = b;
		freezeSet = true;
	}

	@Override
	public void setSpeed(Speed speed)
	{
		rspeed = speed.getSpeed(DistanceUnit.CM, TimeUnit.SECONDS);

	}

	@Override
	public void turn(double delta)
	{
		requestedDeltaHeading = HeadingHelper.normalizeHeading(delta);
		if (requestedDeltaHeading > 180)
		{
			requestedDeltaHeading -= 360;
		}
		logger.info("Requested delta " + delta);

	}

	@Override
	public void publishUpdate()
	{
		if (!freezeSet)
		{
			freeze = false;
		}

		freezeSet = false;

	}

	double hz = 10.0;

	@Override
	public void run()
	{
		long lastScan = 0;

		while (true)
		{
			double deltaDistance = 0;
			double deltaTurn = 0;
			if (!freeze)
			{
				deltaDistance = move(rspeed / hz);
				logger.info("speed " + rspeed);
				logger.info("Delta distance " + deltaDistance);

				double absDelta = Math.abs(requestedDeltaHeading);
				double delta = Math.min(absDelta, 25.0 / hz) * Math.signum(requestedDeltaHeading);

				deltaTurn = internalTurn(delta);
			}
			long to = (long) ((lastScan + (100.0 / hz)));
			if (to > 100)
			{
				to = 100;
			}

			logger.info(lastScan + " " + to);

			List<ScanObservation> observations = getObservation(lastScan, to);
			lastScan = to % 100;

			double headingDrift = Math.abs((random.nextGaussian() * 0.05) * (1.0 / hz));

			for (RobotLocationDeltaListener listener : listeners)
			{
				listener.onMessage(new Angle(deltaTurn + headingDrift, AngleUnits.DEGREES),
						new Distance(deltaDistance, DistanceUnit.CM), observations);
			}
			try
			{
				Thread.sleep((long) (1000.0 / hz));
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

	}

	@Override
	public void addMessageListener(RobotLocationDeltaListener listener)
	{
		this.listeners.add(listener);

	}

	@Override
	public void removeMessageListener(RobotLocationDeltaListener listener)
	{
		this.listeners.remove(listener);

	}

}
