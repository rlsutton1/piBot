package au.com.rsutton.mapping.particleFilter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.pi4j.gpio.extension.lidar.LidarScanner;
import com.pi4j.gpio.extension.lsm303.HeadingData;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.mapping.probability.ProbabilityMap;
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
	private RobotListener listener;

	RobotSimulator(ProbabilityMap map)
	{
		this.map = map;

		new Thread(this, "Robot").start();
	}

	public void move(double distance)
	{
		distance += (distance * (random.nextGaussian() * 0.5));

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
		double noise = (random.nextGaussian() * 0.5) * (1.0 / hz);
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

	public RobotLocation getObservation()
	{

		// System.out.println("Robot x,y,angle " + x + " " + y + " " + heading);
		RobotLocation observation = new RobotLocation();

		List<LidarObservation> observations = new LinkedList<>();

		Particle particle = new Particle(x, y, heading, 2, 2);

		for (double h = LidarScanner.MIN_ANGLE; h < LidarScanner.MAX_ANGLE; h += 5)
		{
			double distance = particle.simulateObservation(map, h, 1000, 0.5);

			if (Math.abs(distance) > 1)
			{
				Vector3D unit = new Vector3D(0, 1, 0).scalarMultiply(distance);
				Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(h));
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

	double hz = 2.0;

	@Override
	public void run()
	{

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
			if (listener != null)
			{
				listener.observed(getObservation());
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
		this.listener = listener;

	}

}
