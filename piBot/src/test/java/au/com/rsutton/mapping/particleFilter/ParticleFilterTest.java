package au.com.rsutton.mapping.particleFilter;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.navigation.RoutePlanner;
import au.com.rsutton.navigation.RoutePlanner.ExpansionPoint;

public class ParticleFilterTest
{

	@Test
	public void test()
	{
		ProbabilityMap map = KitchenMapBuilder.buildKitchenMap();
		map.dumpTextWorld();
		ParticleFilter pf = new ParticleFilter(map, 1000);
		pf.dumpTextWorld(KitchenMapBuilder.buildKitchenMap());

		RoutePlanner routePlanner = new RoutePlanner(map);
		int pfX = 0;
		int pfY = 0;

		RobotSimulator robot = new RobotSimulator(map);
		robot.setLocation(-150, 300, 0);

		routePlanner.createRoute(130, -280);

		double lastAngle = 0;
		int ctr = 0;
		while (true)
		{
			ctr++;

			double da = 5;
			double distance = 0;

			if (pf.getStdDev())
			{
				Vector3D ap = pf.dumpAveragePosition();
				pfX = (int) ap.getX();
				pfY = (int) ap.getY();

				System.out.println("XY " + pfX + " " + pfY);

				ExpansionPoint next = routePlanner.getRouteForLocation(pfX, pfY);
				int dx = next.getX() - pfX;
				int dy = next.getY() - pfY;
				System.out.println(next + " " + dx + " " + dy);
				dx *= 3.0;
				dy *= 3.0;

				if (dx != 0 || dy != 0)
				{
					distance = Vector3D.distance(Vector3D.ZERO, new Vector3D(dx, dy, 0));
					Vector3D delta = new Vector3D(dx, dy, 0);
					double angle = Math.toDegrees(Math.atan2(delta.getY(), delta.getX())) - 90;
					da = HeadingHelper.getChangeInHeading(angle, lastAngle);
					lastAngle = angle;

				} else
				{
					routePlanner.getRouteForLocation(pfX, pfY);
					break;
				}
			}
			robot.turn(da);
			robot.move(distance);

			update(map, pf, distance, da, robot);
			pf.resample(map);
			try
			{
				Thread.sleep(200);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

	}

	private void update(ProbabilityMap map, ParticleFilter pf, final double distance, final double dh,
			RobotSimulator robot)
	{
		pf.moveParticles(new ParticleUpdate()
		{

			@Override
			public double getDeltaHeading()
			{
				return dh;
			}

			@Override
			public double getMoveDistance()
			{
				return distance;
			}
		});

		pf.addObservation(map, robot.getObservation());

		pf.dumpTextWorld(KitchenMapBuilder.buildKitchenMap());
		pf.dumpAveragePosition();

	}

}
