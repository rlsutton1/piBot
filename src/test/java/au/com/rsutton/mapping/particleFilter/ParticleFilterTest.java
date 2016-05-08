package au.com.rsutton.mapping.particleFilter;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.navigation.RoutePlanner;
import au.com.rsutton.navigation.RoutePlanner.ExpansionPoint;
import au.com.rsutton.ui.MainPanel;
import au.com.rsutton.ui.MapDataSource;
import au.com.rsutton.ui.PointSource;
import au.com.rsutton.ui.StatisticSource;

public class ParticleFilterTest
{

	@Test
	public void loopTest()
	{
		List<Long> times = new LinkedList<>();
		for (int i = 0; i < 1; i++)
		{
			long start = System.currentTimeMillis();
			test();
			long elapsed = System.currentTimeMillis() - start;
			times.add(elapsed);
		}

		for (Long time : times)
		{
			System.out.println(time);
		}
	}

	public void test()
	{
		MainPanel ui = new MainPanel();

		ProbabilityMap map = KitchenMapBuilder.buildKitchenMap();
		map.dumpTextWorld();
		ui.addDataSource(map, new Color(255, 255, 255));

		final ParticleFilter pf = new ParticleFilter(map, 1000);
		pf.dumpTextWorld(KitchenMapBuilder.buildKitchenMap());

		ui.addDataSource(pf.getParticlePointSource(), new Color(255, 0, 0));
		ui.addDataSource(pf.getHeadingMapDataSource());

		ui.addStatisticSource(new StatisticSource()
		{

			@Override
			public String getValue()
			{
				return "" + pf.getStdDev();
			}

			@Override
			public String getLabel()
			{
				return "StdDev";
			}
		});

		ui.addStatisticSource(new StatisticSource()
		{

			@Override
			public String getValue()
			{
				return "" + pf.getBestRating();
			}

			@Override
			public String getLabel()
			{
				return "Best Match";
			}
		});

		final RoutePlanner routePlanner = new RoutePlanner(map);
		int pfX = 0;
		int pfY = 0;

		RobotSimulator robot = new RobotSimulator(map);
		robot.setLocation(-150, 300, 0);

		ui.addDataSource(robot);

		routePlanner.createRoute(120, -260);

		ui.addDataSource(new PointSource()
		{

			@Override
			public List<Point> getOccupiedPoints()
			{
				
				// determine the route from the current possition
				Vector3D pos = pf.dumpAveragePosition();
				double x = pos.getX();
				double y = pos.getY();

				List<Point> points = new LinkedList<>();

				for (int i = 0; i < 150; i++)
				{
					ExpansionPoint next = routePlanner.getRouteForLocation((int) x, (int) y);
					points.add(new Point(next.getX(), next.getY()));
					double dx = (x-next.getX())*5;
					x -= dx;
					double dy = (y-next.getY())*5;
					y -= dy;
					if (dx==0 && dy==0)
					{
						// reached the target
						break;
					}
				}

				return points;
			}

		}, new Color(255,255,0));

		double lastAngle = 0;
		int ctr = 0;
		double speed = 0;
		while (true)
		{
			ctr++;

			double da = 5;
			double distance = 0;

			double std = pf.getStdDev();

			if (std < 24)
			{
				speed += 0.05;
			} else if (std > 26)
			{
				speed -= 0.05;
			}
			speed = Math.max(0.01, speed);

			if (std < 30)
			{
				Vector3D ap = pf.dumpAveragePosition();
				pfX = (int) ap.getX();
				pfY = (int) ap.getY();

				lastAngle = pf.getAverageHeading();

				System.out.println("XY " + pfX + " " + pfY);

				ExpansionPoint next = routePlanner.getRouteForLocation(pfX, pfY);

				for (int i = 0; i < 25; i++)
					next = routePlanner.getRouteForLocation(next.getX(), next.getY());

				double dx = next.getX() - pfX;
				double dy = next.getY() - pfY;
				System.out.println(next + " " + dx + " " + dy);
				dx *= speed;
				dy *= speed;

				if (dx != 0 || dy != 0)
				{
					distance = Vector3D.distance(Vector3D.ZERO, new Vector3D(dx, dy, 0));
					Vector3D delta = new Vector3D(dx, dy, 0);
					double angle = Math.toDegrees(Math.atan2(delta.getY(), delta.getX())) - 90;
					if (angle < 0)
					{
						angle += 360;
					}
					if (angle > 360)
					{
						angle -= 360;
					}
					da = HeadingHelper.getChangeInHeading(angle, lastAngle);
					if (Math.abs(da) > 10)
					{
						da = da * (5.0 / Math.abs(da));
					}

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
				Thread.sleep(0);
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

		pf.addObservation(map, robot.getObservation(), 0d);

		pf.dumpTextWorld(KitchenMapBuilder.buildKitchenMap());
		pf.dumpAveragePosition();

	}

}
