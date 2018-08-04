package au.com.rsutton.mapping.particleFilter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.hazelcast.SetMotion;
import au.com.rsutton.mapping.KitchenMapBuilder;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.navigation.router.ExpansionPoint;
import au.com.rsutton.navigation.router.RouteOption;
import au.com.rsutton.navigation.router.RoutePlanner;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.ui.DataSourcePoint;
import au.com.rsutton.ui.DataSourceStatistic;
import au.com.rsutton.ui.MapDrawingWindow;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;
import au.com.rsutton.units.Speed;
import au.com.rsutton.units.Time;

public class ParticleFilterLiveTest
{

	volatile private int stop;

	Angle lastheading;
	volatile double speed;

	// final AtomicDouble currentDeadReconingHeading = new AtomicDouble();

	RobotInterface robot = new RobotImple();
	ParticleFilterImpl pf;

	@Test
	public void test() throws InterruptedException
	{

		MapDrawingWindow ui = new MapDrawingWindow(0, 0, 250);

		final ProbabilityMap world = KitchenMapBuilder.buildKitchenMap();
		ui.addDataSource(world, new Color(255, 255, 255));

		double headingNoise = 1.0; // degrees/second
		pf = new ParticleFilterImpl(world, 1000, 0.75, headingNoise, StartPosition.RANDOM, robot, null);
		// pf.dumpTextWorld(KitchenMapBuilder.buildKitchenMap());

		setupDataSources(ui, pf);
		setupRobotListener();

		RoutePlanner routePlanner = new RoutePlanner(world);
		setupRoutePlanner(ui, pf, routePlanner);

		Integer initialX = null;
		Integer initialY = null;

		routePlanner.createRoute(60, -80, RouteOption.ROUTE_THROUGH_UNEXPLORED);

		int pfX = 0;
		int pfY = 0;

		double lastAngle = 0;
		while (true)
		{

			double da = 5;

			double std = pf.getStdDev();

			speed = Math.max(22 - std, 0);
			// if (std < 24)
			// {
			// speed += 0.25;
			// } else if (std > 29)
			// {
			// speed -= 0.25;
			// }
			speed = Math.max(0.01, speed);
			DistanceXY ap = pf.getXyPosition();

			// if (!mapping)
			{
				if (std < 30)
				{

					pfX = (int) ap.getX().convert(DistanceUnit.CM);
					pfY = (int) ap.getY().convert(DistanceUnit.CM);

					if (initialX == null)
					{
						initialX = pfX;
						initialY = pfY;
					}

					lastAngle = pf.getHeading();

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
						if (Math.abs(da) > 90)
						{
							speed *= 0.0;
						}

					} else
					{
						routePlanner.getRouteForLocation(pfX, pfY);

						if (pfX != initialX && pfY != initialY)
						{
							routePlanner.createRoute(initialX, initialY, RouteOption.ROUTE_THROUGH_UNEXPLORED);
						} else
						{
							break;
						}
					}
					SetMotion motion = new SetMotion();

					motion.setChangeHeading(da);

					if (stop > 0)
					{
						speed = -5;
					}
					motion.setSpeed(new Speed(new Distance(speed, DistanceUnit.CM), Time.perSecond()));
					motion.publish();

				} else
				{
					SetMotion motion = new SetMotion();

					motion.setChangeHeading(15.0);
					motion.setFreeze(false);
					motion.setSpeed(new Speed(new Distance(0, DistanceUnit.CM), Time.perSecond()));
					motion.publish();

				}
			}
			// else
			// {
			// // map builder wants improved localisation, so stop
			//
			// speed = 0;
			// SetMotion motion = new SetMotion();
			// motion.setHeading(HeadingHelper.normalizeHeading(currentDeadReconingHeading.get()));
			// motion.setSpeed(new Speed(new Distance(speed, DistanceUnit.CM),
			// Time.perSecond()));
			// motion.setFreeze(true);
			// motion.publish();
			// }

			try
			{
				Thread.sleep(500);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		SetMotion motion = new SetMotion();

		motion.setChangeHeading(0.0);
		motion.setFreeze(false);
		motion.setSpeed(new Speed(new Distance(0, DistanceUnit.CM), Time.perSecond()));
		motion.publish();

		Thread.sleep(1000);
	}

	private void setupRobotListener()
	{

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

		ui.addStatisticSource(new DataSourceStatistic()
		{

			@Override
			public String getValue()
			{
				return "" + speed;
			}

			@Override
			public String getLabel()
			{
				return "Speed cm/s";
			}
		});

		ui.addStatisticSource(new DataSourceStatistic()
		{

			@Override
			public String getValue()
			{
				String value = "True";
				if (stop <= 0)
				{
					value = "False";
				}
				return "" + value;
			}

			@Override
			public String getLabel()
			{
				return "Proximity Stop";
			}
		});

		ui.addDataSource(new DataSourceMap()
		{

			@Override
			public List<Point> getPoints()
			{
				DistanceXY pos = pf.getXyPosition();
				List<Point> points = new LinkedList<>();
				points.add(new Point((int) pos.getX().convert(DistanceUnit.CM),
						(int) pos.getY().convert(DistanceUnit.CM)));
				return points;

			}

			@Override
			public void drawPoint(BufferedImage image, double pointOriginX, double pointOriginY, double scale,
					double originalX, double originalY)
			{
				// draw line showing which way the bot is facing

				Graphics graphics = image.getGraphics();

				graphics.setColor(new Color(255, 255, 255));

				Vector3D line = new Vector3D(60 * scale, 0, 0);
				line = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(pf.getHeading() + 0)).applyTo(line);

				graphics.drawLine((int) pointOriginX, (int) pointOriginY, (int) (pointOriginX + line.getX()),
						(int) (pointOriginY + line.getY()));

				graphics.setColor(new Color(0, 0, 255));

			}
		});

	}

	private void setupRoutePlanner(MapDrawingWindow ui, final ParticleFilterImpl pf, final RoutePlanner routePlanner)
	{

		ui.addDataSource(new DataSourcePoint()
		{

			@Override
			public List<Point> getOccupiedPoints()
			{

				// determine the route from the current possition
				DistanceXY pos = pf.getXyPosition();
				double x = pos.getX().convert(DistanceUnit.CM);
				double y = pos.getY().convert(DistanceUnit.CM);

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

}
