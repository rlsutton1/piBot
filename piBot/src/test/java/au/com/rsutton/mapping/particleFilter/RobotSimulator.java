package au.com.rsutton.mapping.particleFilter;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;

import com.pi4j.gpio.extension.lsm303.HeadingData;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.navigation.RoutePlanner;
import au.com.rsutton.navigation.RoutePlanner.ExpansionPoint;
import au.com.rsutton.navigation.RoutePlannerTest;
import au.com.rsutton.robot.rover.LidarObservation;

public class RobotSimulator
{

	Random random = new Random();
	private ProbabilityMap map;

	double x;
	double y;

	double heading;

	RobotSimulator(ProbabilityMap map)
	{
		this.map = map;
	}

	public void move(double distance)
	{
		distance += distance + (distance * (random.nextGaussian() * 0.5));
		// random turn
		turn(random.nextGaussian() * distance);
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
		}
	}

	public void turn(double angle)
	{
		heading += angle + (random.nextGaussian() * 0.5);
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

		System.out.println("Robot x,y,angle " + x + " " + y + " " + heading);
		RobotLocation observation = new RobotLocation();

		List<LidarObservation> observations = new LinkedList<>();

		Particle particle = new Particle(x, y, heading);

		for (int h = -180; h < 92; h += 5)
		{
			double distance = particle.simulateObservation(map, h, 500);

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
		System.out.println("Observations " + observations.size());

		observation.addObservations(observations);
		observation.setCompassHeading(new HeadingData((float) heading, 10.0f));
		return observation;
	}

	public void setLocation(int x, int y, int heading)
	{
		this.x = x;
		this.y = y;
		this.heading = heading;

	}

}
