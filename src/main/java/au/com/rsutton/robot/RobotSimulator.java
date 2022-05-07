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

import au.com.rsutton.hazelcast.DataLogLevel;
import au.com.rsutton.hazelcast.DataLogValue;
import au.com.rsutton.hazelcast.LidarScan;
import au.com.rsutton.hazelcast.RobotTelemetry;
import au.com.rsutton.mapping.particleFilter.Particle;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
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
	double absoluteTotalDistance = 0;

	double heading;
	private volatile boolean freeze;
	private double rspeed;
	private List<RobotLocationDeltaListener> listeners = new CopyOnWriteArrayList<>();
	boolean freezeSet = false;

	private RobotLocationDeltaMessagePump messsagePump;

	public RobotSimulator(ProbabilityMapIIFc map, int x, int y)
	{
		this.map = map;
		this.x = x;
		this.y = y;

		while (map.get(x, y) > 0.5)
		{
			this.x = (Math.random() * (map.getMaxX() - map.getMinX())) + map.getMinX();
			this.y = (Math.random() * (map.getMaxY() - map.getMinY())) + map.getMinY();
			logger.warn("Looking for an un-occupied location to start the simulator");
		}

		messsagePump = new RobotLocationDeltaMessagePump(this);

		new Thread(this, "Robot").start();
	}

	volatile boolean bump = false;

	private double move(double distance)
	{
		if (freeze)
		{
			return 0;
		}

		Vector3D unit = new Vector3D(0, distance, 0);
		Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(heading));

		Vector3D location = new Vector3D(x, y, 0);
		Vector3D newLocation = location.add(rotation.applyTo(unit));

		heading += (steeringAngle.getDegrees() / 5.0) * distance;

		double nx = newLocation.getX();
		double ny = newLocation.getY();
		// never drive through a wall
		if (map.get(nx, ny) <= 0.5)
		{
			x = nx;
			y = ny;
			totalDistanceTravelled += distance;
			absoluteTotalDistance += Math.abs(distance);
			bump = false;

			return distance;
		} else
		{
			logger.info("Avoiding wall");
			if (distance > 0)
			{
				// don't register bump going backwards
				bump = true;
			}
			return 0;
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
	public void publishUpdate()
	{
		if (!freezeSet)
		{
			if (freeze == true)
			{
				System.out.println("Clearning freeze flag");
				freeze = false;
			}
		}

		freezeSet = false;

	}

	double hz = 6.0;

	private Angle steeringAngle;

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

			}
			long to = (long) ((lastScan + (100.0 / hz)));
			if (to > 100)
			{
				to = 100;
			}

			logger.info(lastScan + " " + to);

			List<LidarObservation> observations = getObservation(0, 100);
			lastScan = to % 100;

			RobotTelemetry message = new RobotTelemetry();
			message.setDeadReaconingHeading(new Angle(360 - heading, AngleUnits.DEGREES));
			message.setDistanceTravelled(new Distance(totalDistanceTravelled, DistanceUnit.CM));
			message.setAbsoluteTotalDistance(new Distance(absoluteTotalDistance, DistanceUnit.CM));
			message.setBumpLeft(bump);
			message.setBumpRight(bump);
			new DataLogValue("Simulator-Distance Traveled", "" + totalDistanceTravelled, DataLogLevel.INFO).publish();

			new DataLogValue("Simulator-Angle turned", "" + heading, DataLogLevel.INFO).publish();

			LidarScan scan = new LidarScan();
			scan.setStartTime(System.currentTimeMillis());
			scan.setEndTime(System.currentTimeMillis());
			scan.setObservations(observations);

			messsagePump.onMessage(message);
			messsagePump.onMessage(scan);

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
	public void onMessage(Angle deltaHeading, Distance deltaDistance, boolean bump, Distance absoluteTotalDistance)
	{
		for (RobotLocationDeltaListener listener : listeners)
		{

			listener.onMessage(deltaHeading, deltaDistance, bump, absoluteTotalDistance);
		}

	}

	@Override
	public void onMessage(LidarScan robotLocation)
	{
		for (RobotLocationDeltaListener listener : listeners)
		{

			listener.onMessage(robotLocation);
		}

	}

	@Override
	public double getPlatformRadius()
	{
		return 15;
	}

	@Override
	public void setSteeringAngle(Angle normalizeHeading)
	{
		steeringAngle = normalizeHeading;
		new DataLogValue("Simulator-Requested turnRadius", "" + normalizeHeading.getDegrees(), DataLogLevel.INFO)
				.publish();

	}
}
