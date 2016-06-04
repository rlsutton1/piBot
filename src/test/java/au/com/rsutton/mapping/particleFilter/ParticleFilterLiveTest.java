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
import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.hazelcast.SetMotion;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.navigation.RoutePlanner;
import au.com.rsutton.navigation.RoutePlanner.ExpansionPoint;
import au.com.rsutton.robot.rover.Angle;
import au.com.rsutton.robot.rover.AngleUnits;
import au.com.rsutton.ui.MainPanel;
import au.com.rsutton.ui.MapDataSource;
import au.com.rsutton.ui.PointSource;
import au.com.rsutton.ui.StatisticSource;

import com.google.common.util.concurrent.AtomicDouble;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

public class ParticleFilterLiveTest
{
	volatile double compassHeading = 0;

	volatile private int stop;

	Angle lastheading;
	volatile double speed;

	final AtomicDouble currentDeadReconingHeading = new AtomicDouble();

	private MapBuilder mapBuilder;

	volatile private boolean mapping = false;
	private double sampleSize = 1.0;

	@Test
	public void test() throws InterruptedException
	{

		MainPanel ui = new MainPanel();

		mapBuilder = new MapBuilder();

		final ProbabilityMap world = KitchenMapBuilder.buildKitchenMap();
		ui.addDataSource(world, new Color(255, 255, 255));

		final ParticleFilter pf = new ParticleFilter(world, 2000, 1.75, 0.75);
		// pf.dumpTextWorld(KitchenMapBuilder.buildKitchenMap());

		setupDataSources(ui, pf);
		setupRobotListener(currentDeadReconingHeading, world, pf);

		RoutePlanner routePlanner = new RoutePlanner(world);
		setupRoutePlanner(ui, pf, routePlanner);

		int pfX = 0;
		int pfY = 0;

		double lastAngle = 0;
		while (true)
		{

			double da = 5;
			double distance = 0;

			double std = pf.getStdDev();

			if (std < 24)
			{
				speed += 0.25;
			} else if (std > 29)
			{
				speed -= 0.25;
			}
			speed = Math.max(0.01, speed);
			pf.setParticleCount(Math.max(200, (int) (7 * std)));
			Vector3D ap = pf.dumpAveragePosition();
			mapping = !mapBuilder.canMove(speed, ap);

			// if (!mapping)
			{
				if (std < 30)
				{

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
						if (Math.abs(da) > 30)
						{
							speed *= 0.8;
						}

					} else
					{
						routePlanner.getRouteForLocation(pfX, pfY);
						break;
					}
					SetMotion motion = new SetMotion();

					motion.setHeading(HeadingHelper.normalizeHeading(currentDeadReconingHeading.get() + da));

					if (stop > 0)
					{
						speed = -5;
					}
					motion.setSpeed(new Speed(new Distance(speed, DistanceUnit.CM), Time.perSecond()));
					motion.publish();

				} else
				{
					SetMotion motion = new SetMotion();

					motion.setHeading(HeadingHelper.normalizeHeading(currentDeadReconingHeading.get() + 5));
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

		motion.setHeading(HeadingHelper.normalizeHeading(currentDeadReconingHeading.get()));
		motion.setFreeze(false);
		motion.setSpeed(new Speed(new Distance(0, DistanceUnit.CM), Time.perSecond()));
		motion.publish();

		Thread.sleep(1000);
	}

	private void setupRobotListener(final AtomicDouble currentDeadReconingHeading, final ProbabilityMap world,
			final ParticleFilter pf)
	{
		new RobotLocation().addMessageListener(new MessageListener<RobotLocation>()
		{

			Double lastx = null;
			Double lasty = null;
			private int updateCounter;

			@Override
			public void onMessage(Message<RobotLocation> message)
			{

				final RobotLocation robotLocation = message.getMessageObject();
				compassHeading = robotLocation.getCompassHeading().getHeading();

				currentDeadReconingHeading.set(robotLocation.getDeadReaconingHeading().getDegrees());

				pf.addObservation(world, robotLocation, -90d);
				if (lastx != null)
				{
					pf.moveParticles(new ParticleUpdate()
					{

						@Override
						public double getDeltaHeading()
						{
							return lastheading.difference(new Angle(HeadingHelper.normalizeHeading(robotLocation
									.getDeadReaconingHeading().getDegrees()), AngleUnits.DEGREES));
						}

						@Override
						public double getMoveDistance()
						{
							double dx = (lastx - robotLocation.getX().convert(DistanceUnit.CM));
							double dy = (lasty - robotLocation.getY().convert(DistanceUnit.CM));
							return Vector3D.distance(Vector3D.ZERO, new Vector3D(dx, dy, 0));
						}
					});
				}
				lasty = robotLocation.getY().convert(DistanceUnit.CM);
				lastx = robotLocation.getX().convert(DistanceUnit.CM);
				lastheading = robotLocation.getDeadReaconingHeading();

				boolean resample = false;
				for (ScanObservation obs : robotLocation.getObservations())
				{
					if (obs.isStartOfScan())
					{
						// reduce stop counter
						resample = true;
						stop--;
					}
					if (Vector3D.distance(Vector3D.ZERO, obs.getVector()) < 20)
					{
						// stop for 2 full sweeps
						stop = 1;
					}

				}

				if (!mapping || resample)
				{
					System.out.println("Resample >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
					updateCounter++;
					if (updateCounter >= sampleSize)
					{
						updateCounter = 0;

						pf.resample(world);
					}
				}
				// pf.dumpTextWorld(KitchenMapBuilder.buildKitchenMap());

			}
		});
	}

	private void setupDataSources(MainPanel ui, final ParticleFilter pf)
	{
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
				return "" + compassHeading;
			}

			@Override
			public String getLabel()
			{
				return "compass Heading";
			}
		});

		ui.addStatisticSource(new StatisticSource()
		{

			@Override
			public String getValue()
			{
				return "" + currentDeadReconingHeading;
			}

			@Override
			public String getLabel()
			{
				return "deadReconning Heading";
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

		ui.addStatisticSource(new StatisticSource()
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

		ui.addStatisticSource(new StatisticSource()
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

		ui.addDataSource(new MapDataSource()
		{

			@Override
			public List<Point> getPoints()
			{
				Vector3D pos = pf.dumpAveragePosition();
				List<Point> points = new LinkedList<>();
				points.add(new Point((int) pos.getX(), (int) pos.getY()));
				return points;

			}

			@Override
			public void drawPoint(BufferedImage image, double pointOriginX, double pointOriginY, double scale)
			{
				Graphics graphics = image.getGraphics();

				graphics.setColor(new Color(255, 255, 255));

				Vector3D line = new Vector3D(60 * scale, 0, 0);
				line = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(compassHeading + 0)).applyTo(line);

				graphics.drawLine((int) pointOriginX, (int) pointOriginY, (int) (pointOriginX + line.getX()),
						(int) (pointOriginY + line.getY()));

				graphics.setColor(new Color(0, 0, 255));

			}
		});
	}

	private void setupRoutePlanner(MainPanel ui, final ParticleFilter pf, final RoutePlanner routePlanner)
	{
		routePlanner.createRoute(110, -250);

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
