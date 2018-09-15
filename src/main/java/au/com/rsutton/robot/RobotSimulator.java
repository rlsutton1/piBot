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
import au.com.rsutton.hazelcast.DataLogValue;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.mapping.particleFilter.Particle;
import au.com.rsutton.mapping.particleFilter.ScanObservation;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.navigation.feature.RobotLocationDeltaListener;
import au.com.rsutton.navigation.feature.RobotLocationDeltaMessagePump;
import au.com.rsutton.robot.lidar.LidarObservation;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.AngleUnits;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;
import au.com.rsutton.units.Speed;

public class RobotSimulator implements DataSourceMap, RobotInterface, Runnable, RobotLocationDeltaListener
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

	private RobotLocationDeltaMessagePump messsagePump;

	public RobotSimulator(ProbabilityMapIIFc map)
	{
		this.map = map;

		messsagePump = new RobotLocationDeltaMessagePump(this);

		new Thread(this, "Robot").start();
	}

	volatile boolean bump = false;

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
			bump = false;

			return distance;
		} else
		{
			logger.info("Avoiding wall");
			bump = true;
			return 0;
		}

	}

	public void internalTurn(double angle)
	{
		if (freeze)
		{
			return;
		}
		double noise = Math.abs((random.nextGaussian() * 1.5) * (1.0 / hz));
		double delta = angle + noise;
		heading -= delta;
		if (heading < 0)
		{
			heading += 360.0;
		}
		if (heading > 360)
		{
			heading -= 360.0;
		}

	}

	/**
	 * simulates a full scan per second
	 * 
	 * @param msSinceLastScan
	 * @return
	 */
	public List<LidarObservation> getObservation(double fromPercentage, double toPercentage)
	{

		List<LidarObservation> observations = new LinkedList<>();

		Particle particle = new Particle(x, y, heading, 2, 2);

		Random rand = new Random();
		double stepSize = 1.9;
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
				observations.add(new LidarObservation(distanceVector));
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
	public void drawPoint(BufferedImage image, double pointOriginX, double pointOriginY, double scale, double originalX,
			double originalY)
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

		new DataLogValue("Simulator-Requested angle change", "" + delta).publish();

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

	double hz = 6.0;

	@Override
	public void run()
	{
		long lastScan = 0;

		while (true)
		{
			double deltaDistance = 0;
			if (!freeze)
			{
				deltaDistance = move(rspeed / hz);
				logger.info("speed " + rspeed);
				logger.info("Delta distance " + deltaDistance);

				double absDelta = Math.abs(requestedDeltaHeading);

				// max rate of turn is 90 degrees per second
				double delta = Math.min(absDelta, 90.0 / hz) * Math.signum(requestedDeltaHeading);

				internalTurn(delta);
			}
			long to = (long) ((lastScan + (100.0 / hz)));
			if (to > 100)
			{
				to = 100;
			}

			logger.info(lastScan + " " + to);

			List<LidarObservation> observations = getObservation(0, 100);
			lastScan = to % 100;

			RobotLocation message = new RobotLocation();
			message.setDeadReaconingHeading(new Angle(360 - heading, AngleUnits.DEGREES));
			message.setDistanceTravelled(new Distance(totalDistanceTravelled, DistanceUnit.CM));
			message.setBumpLeft(bump);
			message.setBumpRight(bump);
			new DataLogValue("Simulator-Distance Traveled", "" + totalDistanceTravelled).publish();

			new DataLogValue("Simulator-Angle turned", "" + heading).publish();

			message.setObservations(observations);
			messsagePump.onMessage(message);

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

	@Override
	public void onMessage(Angle deltaHeading, Distance deltaDistance, List<ScanObservation> robotLocation, boolean bump)
	{
		for (RobotLocationDeltaListener listener : listeners)
		{

			listener.onMessage(deltaHeading, deltaDistance, robotLocation, bump);
		}

	}

	@Override
	public double getRadius()
	{
		return 15;
	}

}
