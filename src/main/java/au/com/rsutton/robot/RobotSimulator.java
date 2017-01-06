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

import com.pi4j.gpio.extension.lsm303.HeadingData;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.mapping.particleFilter.InitialWorldBuilder;
import au.com.rsutton.mapping.particleFilter.Particle;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.robot.lidar.Spinner;
import au.com.rsutton.robot.rover.Angle;
import au.com.rsutton.robot.rover.AngleUnits;
import au.com.rsutton.robot.rover.LidarObservation;
import au.com.rsutton.ui.DataSourceMap;

public class RobotSimulator implements DataSourceMap, RobotInterface, Runnable
{

	Random random = new Random();
	private ProbabilityMap map;

	double x;
	double y;

	double heading;
	private volatile boolean freeze;
	private double rspeed;
	private double targetHeading;
	private List<RobotListener> listeners = new CopyOnWriteArrayList<>();

	public RobotSimulator(ProbabilityMap map)
	{
		this.map = map;

		new Thread(this, "Robot").start();
	}

	public void move(double distance)
	{
		distance -= Math.abs((distance * (random.nextGaussian() * 0.5)));

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
		} else
		{
			System.out.println("Avoiding wall");
		}
	}

	public void turn(double angle)
	{
		double noise = Math.abs((random.nextGaussian() * 0.5) * (1.0 / hz));
		heading += angle + noise;
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
	public RobotLocation getObservation(double fromPercentage, double toPercentage)
	{

		// System.out.println("Robot x,y,angle " + x + " " + y + " " + heading);
		RobotLocation observation = new RobotLocation();

		List<LidarObservation> observations = new LinkedList<>();

		Particle particle = new Particle(x, y, heading, 2, 2);

		Random rand = new Random();
		double stepSize = 3.6;
		double stepNoise = 1.2;

		for (double h = (int) (Spinner.getMinAngle() * (fromPercentage / 100.0)); h < (int) (Spinner.getMaxAngle()
				* (toPercentage / 100.0)); h += stepSize + (rand.nextGaussian() * stepNoise))
		{
			double adjustedHeading = h - 180 + 45;
			double distance = particle.simulateObservation(map, adjustedHeading, 1000,
					InitialWorldBuilder.REQUIRED_POINT_CERTAINTY);

			if (Math.abs(distance) > 1)
			{
				Vector3D unit = new Vector3D(0, 1, 0).scalarMultiply(distance);
				Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(adjustedHeading));
				Vector3D distanceVector = rotation.applyTo(unit);
				observations.add(new LidarObservation(distanceVector, true));
			}
		}

		if (observations.isEmpty())
		{
			System.out.println("NO observations!");
		}
		// System.out.println("Observations " + observations.size());

		observation.addObservations(observations);

		observation.setCompassHeading(new HeadingData((float) heading + 90, 10.0f));
		observation.setDeadReaconingHeading(new Angle((float) heading, AngleUnits.DEGREES));
		observation.setX(new Distance(x, DistanceUnit.CM));
		observation.setY(new Distance(y, DistanceUnit.CM));
		return observation;
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

	boolean freezeSet = false;

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
	public void setHeading(double normalizeHeading)
	{
		targetHeading = normalizeHeading;

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
			if (!freeze)
			{
				move(rspeed / hz);
				System.out.println(rspeed);

				double delta = HeadingHelper.getChangeInHeading(heading, targetHeading - 180);

				double absDelta = Math.abs(delta);
				delta = Math.min(absDelta, (25.0 / hz)) * Math.signum(delta);

				turn(delta);
			}
			long to = (long) ((lastScan + (100.0 / hz)) % 100);
			if (to < lastScan)
			{
				lastScan = 0;
			}

			RobotLocation observation = getObservation(lastScan, to);
			lastScan = to;

			for (RobotListener listener : listeners)
			{
				listener.observed(observation);
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
	public void addMessageListener(RobotListener listener)
	{
		this.listeners.add(listener);

	}

	@Override
	public void removeMessageListener(RobotListener listener)
	{
		this.listeners.remove(listener);

	}

}
