package au.com.rsutton.mapping.particleFilter;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.security.acl.LastOwnerException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;

import com.google.common.util.concurrent.AtomicDouble;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

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
import au.com.rsutton.robot.rover.LidarObservation;
import au.com.rsutton.ui.MainPanel;
import au.com.rsutton.ui.MapDataSource;
import au.com.rsutton.ui.PointSource;
import au.com.rsutton.ui.StatisticSource;

public class ParticleFilterLiveTest
{
	volatile double compassHeading = 0;

	volatile private int stop;

	Angle lastheading;
	volatile double speed;

	@Test
	public void test() throws InterruptedException
	{

		MainPanel ui = new MainPanel();

		final AtomicDouble currentDeadReconingHeading = new AtomicDouble();

		final ProbabilityMap world = KitchenMapBuilder.buildKitchenMap();
		ui.addDataSource(world, new Color(255, 255, 255));

		final ParticleFilter pf = new ParticleFilter(world, 2000);
		// pf.dumpTextWorld(KitchenMapBuilder.buildKitchenMap());

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
		new RobotLocation().addMessageListener(new MessageListener<RobotLocation>()
		{

			Double lastx = null;
			Double lasty = null;

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
							return lastheading.difference(robotLocation.getDeadReaconingHeading());
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
				for (LidarObservation obs : robotLocation.getObservations())
				{
					if (obs.isStartOfScan())
					{
						// reduce stop counter
						resample = true;
						stop--;
					}
					if (Vector3D.distance(Vector3D.ZERO, obs.getVector()) < 25)
					{
						// stop for 2 full sweeps
						stop = 3;
					}

				}

				// if (resample)
				{
					pf.resample(world);
				}
				// pf.dumpTextWorld(KitchenMapBuilder.buildKitchenMap());

			}
		});
		;

		RoutePlanner routePlanner = new RoutePlanner(world);
		routePlanner.createRoute(110, -250);

		int pfX = 0;
		int pfY = 0;

		double lastAngle = 0;
		int ctr = 0;
		while (true)
		{
			ctr++;

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
						// da = da * (5.0 / Math.abs(da));
					}

				} else
				{
					routePlanner.getRouteForLocation(pfX, pfY);
					break;
				}
				SetMotion motion = new SetMotion();

				motion.setHeading(currentDeadReconingHeading.get() + da);

				if (stop > 0)
				{
					speed = -5;
				}
				motion.setSpeed(new Speed(new Distance(speed, DistanceUnit.CM), Time.perSecond()));
				motion.publish();
			} else
			{
				SetMotion motion = new SetMotion();

				motion.setHeading(currentDeadReconingHeading.get() + 5);
				motion.setFreeze(false);
				motion.setSpeed(new Speed(new Distance(0, DistanceUnit.CM), Time.perSecond()));
				motion.publish();

			}

			try
			{
				Thread.sleep(500);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		SetMotion motion = new SetMotion();

		motion.setHeading(currentDeadReconingHeading.get() + 5);
		motion.setFreeze(false);
		motion.setSpeed(new Speed(new Distance(0, DistanceUnit.CM), Time.perSecond()));
		motion.publish();

		Thread.sleep(1000);
	}

}
