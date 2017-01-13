package au.com.rsutton.mapping.particleFilter;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;

import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.mapping.KitchenMapBuilder;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.navigation.router.ExpansionPoint;
import au.com.rsutton.navigation.router.RouteOption;
import au.com.rsutton.navigation.router.RoutePlanner;
import au.com.rsutton.robot.rover.LidarObservation;

public class RobotSimulatorTest
{

	@Test
	public void test()
	{
		ProbabilityMap map = KitchenMapBuilder.buildKitchenMap();
		map.dumpTextWorld();

		RoutePlanner routePlanner = new RoutePlanner(map);
		int x = -100;
		int y = 300;

		routePlanner.createRoute(30, -140, RouteOption.ROUTE_THROUGH_UNEXPLORED);

		while (true)
		{

			ExpansionPoint next = routePlanner.getRouteForLocation(x, y);
			int dx = next.getX() - x;
			int dy = next.getY() - y;

			if (dx != 0 || dy != 0)
			{

				Vector3D delta = new Vector3D(dx, dy, 0);

				double angle = Math.toDegrees(Math.atan2(delta.getY(), delta.getX())) - 90;

				Particle particle = new Particle(x, y, angle, 2, 2);
				update(map, particle, x, y, angle);
				System.out.println("Heading " + angle + " Rating " + particle.getRating());

				x += dx;
				y += dy;
			} else
			{
				break;
			}
		}

	}

	private void update(ProbabilityMap map, Particle testParticle, double x, double y, final double heading)
	{

		RobotLocation observation = new RobotLocation();

		List<LidarObservation> observations = new LinkedList<>();

		Particle particle = new Particle(x, y, heading, 2, 2);

		for (int h = -80; h < 88; h += 5)
		{
			double distance = particle.simulateObservation(map, h, 500, 0.5);

			if (Math.abs(distance) > 1)
			{
				Vector3D unit = new Vector3D(0, 1, 0).scalarMultiply(distance);
				Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(h));
				Vector3D distanceVector = rotation.applyTo(unit);
				observations.add(new LidarObservation(distanceVector, true));
			}
		}

		observation.addObservations(observations);
		testParticle.addObservation(map, observation, 0d, true);

		// atan2 I think is 90 degrees out of phase with y = 0 degrees omg how
		// much code will be broken as a result of that!!!!

	}

}
