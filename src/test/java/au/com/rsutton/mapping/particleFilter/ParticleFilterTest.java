package au.com.rsutton.mapping.particleFilter;

import java.awt.Color;
import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.mapping.KitchenMapBuilder;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.navigation.router.ExpansionPoint;
import au.com.rsutton.navigation.router.RouteOption;
import au.com.rsutton.navigation.router.RoutePlanner;
import au.com.rsutton.robot.RobotSimulator;
import au.com.rsutton.robot.rover.Angle;
import au.com.rsutton.robot.rover.MovingLidarObservationMultiBuffer;
import au.com.rsutton.ui.DataSourcePoint;
import au.com.rsutton.ui.DataSourceStatistic;
import au.com.rsutton.ui.MapDrawingWindow;

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
		MapDrawingWindow ui = new MapDrawingWindow();

		ProbabilityMap map = KitchenMapBuilder.buildKitchenMap();
		map.dumpTextWorld();
		ui.addDataSource(map, new Color(255, 255, 255));

		final ParticleFilterImpl pf = new ParticleFilterImpl(map, 2000, 2, 4, StartPosition.RANDOM);
		pf.dumpTextWorld(KitchenMapBuilder.buildKitchenMap());

		setupDataSources(ui, pf);

		final RoutePlanner routePlanner = new RoutePlanner(map);
		int pfX = 0;
		int pfY = 0;

		RobotSimulator robot = new RobotSimulator(map);
		robot.setLocation(-150, 300, 0);

		ui.addDataSource(robot);

		setupRoutePlanner(ui, pf, routePlanner);

		double lastAngle = 0;
		double speed = 0;
		while (true)
		{

			double da = 5;
			double distance = 0;

			double std = pf.getStdDev();

			if (std < 18)
			{
				speed += 0.05;

			} else if (std > 24)
			{
				speed -= 0.05;
			}

			pf.setParticleCount(Math.max(200, (int) (7 * std)));

			speed = Math.max(0.01, speed);
			speed = Math.min(0.15, speed);

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
						da = 10 * Math.signum(da);
					}

				} else
				{
					routePlanner.getRouteForLocation(pfX, pfY);
					System.out.println("arrived at " + pfX + " " + pfY);
					break;
				}

			}
			robot.turn(da);

			robot.move(distance);

			update(map, pf, distance, da, robot);

			try
			{
				Thread.sleep(200);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

	}

	private void setupDataSources(MapDrawingWindow ui, final ParticleFilterImpl pf)
	{
		ui.addDataSource(pf.getParticlePointSource(), new Color(255, 0, 0));
		ui.addDataSource(pf.getHeadingMapDataSource());

		ui.addStatisticSource(new DataSourceStatistic()
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

		ui.addStatisticSource(new DataSourceStatistic()
		{

			@Override
			public String getValue()
			{
				return "" + pf.getBestScanMatchScore();
			}

			@Override
			public String getLabel()
			{
				return "Best Match";
			}
		});
	}

	private void setupRoutePlanner(MapDrawingWindow ui, final ParticleFilterImpl pf, final RoutePlanner routePlanner)
	{
		routePlanner.createRoute(120, -260, RouteOption.ROUTE_THROUGH_UNEXPLORED);

		ui.addDataSource(new DataSourcePoint()
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
					double dx = (x - next.getX()) * 5;
					x -= dx;
					double dy = (y - next.getY()) * 5;
					y -= dy;
					if (dx == 0 && dy == 0)
					{
						// reached the target
						break;
					}
				}

				return points;
			}

		}, new Color(255, 255, 0));
	}

	double lastDeadreconningHeading = 0;

	private void update(ProbabilityMapIIFc map, ParticleFilterImpl pf, final double distance, final double dh,
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

		MovingLidarObservationMultiBuffer buffer = new MovingLidarObservationMultiBuffer(2);
		final RobotLocation data = robot.getObservation(0, 100);

		storeHeadingDeltas(data);
		buffer.addObservation(data);

		ParticleFilterObservationSet nd = new ParticleFilterObservationSet()
		{

			@Override
			public List<ScanObservation> getObservations()
			{
				List<ScanObservation> result = new LinkedList<>();
				result.addAll(buffer.getObservations(data));
				return result;
			}

			@Override
			public Angle getDeadReaconingHeading()
			{
				return data.getDeadReaconingHeading();
			}

		};

		pf.addObservation(map, nd, -90d);

		pf.dumpTextWorld(KitchenMapBuilder.buildKitchenMap());
		pf.dumpAveragePosition();

	}

	private void storeHeadingDeltas(final RobotLocation robotLocation)
	{
		lastDeadreconningHeading = robotLocation.getDeadReaconingHeading().getDegrees();
	}

}
