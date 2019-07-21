package au.com.rsutton.navigation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.hazelcast.DataLogLevel;
import au.com.rsutton.hazelcast.DataLogValue;
import au.com.rsutton.kalman.RobotPoseSourceTimeTraveling;
import au.com.rsutton.mapping.particleFilter.RobotPoseSource;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.navigation.router.ExpansionPoint;
import au.com.rsutton.navigation.router.RouteOption;
import au.com.rsutton.navigation.router.RoutePlanner;
import au.com.rsutton.navigation.router.RoutePlannerLastMeter;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.robot.roomba.Roomba630;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.ui.DataSourcePoint;
import au.com.rsutton.ui.DataSourceStatistic;
import au.com.rsutton.ui.MapDrawingWindow;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;
import au.com.rsutton.units.Speed;
import au.com.rsutton.units.Time;

public class Navigator implements Runnable, NavigatorControl
{

	private static final int BEZIER_SAMPLING_DISTANCE = 5;

	private final int MAX_SPEED;

	Logger logger = LogManager.getLogger();

	private RobotPoseSource pf;
	private RoutePlanner routePlanner;
	private RobotInterface robot;
	private MapDrawingWindow ui;
	private ScheduledExecutorService pool;
	private volatile boolean stopped = true;
	int pfX = 0;
	int pfY = 0;
	Integer initialX;
	Integer initialY;
	double lastAngle;
	private boolean reachedDestination = false;

	/**
	 * if target heading is null, any orientation will do
	 */
	private Double targetHeading;

	double speed = 0;

	private volatile int stuckCounter = 0;

	// private Double initialHeading = null;

	public Navigator(ProbabilityMapIIFc map2, RobotPoseSourceTimeTraveling pf, RobotInterface robot, int maxSpeed)
	{
		this.MAX_SPEED = maxSpeed;
		ui = new MapDrawingWindow("Navigator", 600, 0, 250, true);
		ui.addDataSource(map2, new Color(255, 255, 255));

		this.pf = pf;
		setupDataSources(ui, pf);

		routePlanner = new RoutePlannerLastMeter(map2, robot, pf);

		// add data source from depth camera
		ui.addDataSource((DataSourceMap) routePlanner);

		this.robot = robot;

		setupRoutePlanner(map2);

		setupRobotListener();

		pool = Executors.newScheduledThreadPool(1);

		pool.scheduleWithFixedDelay(this, 500, 100, TimeUnit.MILLISECONDS);

	}

	@Override
	public void run()
	{
		try
		{

			if (stopped)
			{
				robot.freeze(true);
				return;
			}
			if (!routePlanner.hasPlannedRoute())
			{
				robot.freeze(true);
				return;
			}
			robot.freeze(false);
			speed = MAX_SPEED;

			double std = pf.getStdDev();

			if (std < 60)
			{
				// the partical filter is sufficently localized
				DistanceXY ap = pf.getXyPosition();
				pfX = (int) ap.getX().convert(DistanceUnit.CM);
				pfY = (int) ap.getY().convert(DistanceUnit.CM);

				if (initialX == null)
				{
					// record our starting position, once the particle filter is
					// localized the first time
					initialX = pfX;
					initialY = pfY;
				}
				lastAngle = pf.getHeading();

				ExpansionPoint next = new ExpansionPoint(pfX, pfY, 0, null);

				List<Point2D> vals = new LinkedList<>();

				double turnRadius = Roomba630.STRAIGHT;

				ExpansionPoint current = next;
				// get a point on the route 25 steps from where we are, we
				// look ahead (25 steps) so our heading remains reasonably
				// stable
				next = routePlanner.getRouteForLocation(pfX, pfY);

				if (current.equals(next))
				{
					robot.freeze(true);
					new DataLogValue("Navigator routing", "cant route", DataLogLevel.ERROR).publish();
					stuckCounter++;
					return;
				}
				stuckCounter = 0;
				new DataLogValue("Navigator routing", "routing", DataLogLevel.INFO).publish();

				addPointBehindRobotForBezier(vals);

				int i = 0;
				int lookAheadCM = 300;
				for (; i < lookAheadCM; i++)
				{
					next = routePlanner.getRouteForLocation(next.getX(), next.getY());
					if (i == 0 || i % BEZIER_SAMPLING_DISTANCE == 0)
					{
						vals.add(new Point2D.Double(next.getX(), next.getY()));
					}
				}
				vals.add(new Point2D.Double(next.getX(), next.getY()));

				try
				{
					Bezier bezier = Bezier.createBezier(vals);

					turnRadius = bezier.getRadiusAtPosition(0 + (1.0 / vals.size()), (1.0 / (vals.size() + 1)) * 2.0);
					turnRadius /= 2.0;

				} catch (Exception e)
				{
					e.printStackTrace();
				}

				double distanceToTarget = routePlanner.getDistanceToTarget(pfX, pfY);

				// slow as we approach the target
				speed = Math.min(Math.max(3, distanceToTarget), speed);

				if (distanceToTarget > 20)
				{
					// follow the route to the target

					speed = setHeadingWithObsticleAvoidance(turnRadius, speed);
					robot.setSpeed(new Speed(new Distance(speed, DistanceUnit.CM), Time.perSecond()));
					System.out.println("1 Set speed to " + speed);
				} else
				{
					// we have arrived at our target location
					speed = 0;
					if (targetHeading != null
							&& Math.abs(HeadingHelper.getChangeInHeading(lastAngle, targetHeading)) > 5)
					{
						// turn on the spot to set our heading
						double da = -HeadingHelper.getChangeInHeading(targetHeading, lastAngle);
						robot.setSpeed(new Speed(new Distance(5, DistanceUnit.CM), Time.perSecond()));
						System.out.println("2 Set speed to " + speed);
						robot.setTurnRadius(Math.signum(da));

					} else
					{
						System.out.println("3 Set speed to 0");
						// were done, stop and shutdown
						stopped = true;
						speed = 0;
						reachedDestination = true;
						robot.freeze(true);
						Thread.sleep(500);
					}
				}

			} else
			{
				// particle filter isn't localised, just turn on the spot
				speed = setHeadingWithObsticleAvoidance(HeadingHelper.normalizeHeading(0), 10);
				robot.setStraight("Nav - unlocal");
				robot.setSpeed(new Speed(new Distance(0, DistanceUnit.CM), Time.perSecond()));

			}
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			robot.publishUpdate();
		}

	}

	double currentRadius = Roomba630.STRAIGHT;

	void adjustRadius(double newRadius)
	{
		if (Math.signum(currentRadius) == Math.signum(newRadius))
		{
			currentRadius = (currentRadius * 0.35) + (newRadius * 0.65);
		} else
		{
			// almost straight in and change the sign
			currentRadius = (Roomba630.STRAIGHT - 1) * Math.signum(newRadius);
		}
	}

	double setHeadingWithObsticleAvoidance(double desiredTurnRadius, double desiredSpeed)
	{
		double setRadis = desiredTurnRadius;

		if (Math.abs(desiredTurnRadius) >= Roomba630.STRAIGHT || desiredTurnRadius == 0)
		{

			robot.setStraight("NAV set Head");
			new DataLogValue("Desired Turn Radius", "Straight NAV", DataLogLevel.INFO).publish();
			return desiredSpeed;
		}
		if (Math.abs(desiredTurnRadius) < 1)
		{
			setRadis = Math.signum(desiredTurnRadius);
		}
		setRadis = -1.0 * setRadis;
		new DataLogValue("Desired Turn Radius", "" + setRadis, DataLogLevel.INFO).publish();
		adjustRadius(setRadis);
		robot.setTurnRadius(currentRadius);

		// cap speed so we dont exceed maxturnsPerSecond
		double maxTurnsPerSecond = 0.25;

		double maxSpeed = maxTurnsPerSecond * 2 * Math.PI * Math.abs(desiredTurnRadius);
		if (maxSpeed < 10)
		{
			maxSpeed = 10;
		}

		return Math.min(desiredSpeed, maxSpeed);
	}

	private void setupRobotListener()
	{

	}

	private void setupRoutePlanner(ProbabilityMapIIFc worldMap)
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
				if (routePlanner.hasPlannedRoute())
				{

					for (int i = 0; i < 3000; i++)
					{
						ExpansionPoint next = ((RoutePlannerLastMeter) routePlanner).getMasterRouteForLocation((int) x,
								(int) y);

						points.add(new Point(next.getX(), next.getY()));

						double dx = (x - next.getX());
						x -= dx;
						double dy = (y - next.getY());
						y -= dy;
						if (dx == 0 && dy == 0)
						{
							// reached the target
							points.add(new Point(next.getX(), next.getY()));
							break;
						}

					}

				}

				return points;
			}

		}, new Color(0, 255, 0));

		ui.addDataSource(new DataSourcePoint()
		{

			@Override
			public List<Point> getOccupiedPoints()
			{

				// determine the route from the current possition
				DistanceXY pos = pf.getXyPosition();
				double x = pos.getX().convert(DistanceUnit.CM);
				double y = pos.getY().convert(DistanceUnit.CM);

				List<Point2D> vals = new LinkedList<>();

				addPointBehindRobotForBezier(vals);

				List<Point> points2 = new LinkedList<>();
				List<Point> points = new LinkedList<>();
				if (routePlanner.hasPlannedRoute())
				{

					for (int i = 0; i < 3000; i++)
					{
						ExpansionPoint next = routePlanner.getRouteForLocation((int) x, (int) y);
						points.add(new Point(next.getX(), next.getY()));

						if (i % BEZIER_SAMPLING_DISTANCE == 0)
						{
							vals.add(new Point2D.Double(next.getX(), next.getY()));
						}

						double dx = (x - next.getX());
						x -= dx;
						double dy = (y - next.getY());
						y -= dy;
						if (dx == 0 && dy == 0)
						{
							// reached the target
							vals.add(new Point2D.Double(next.getX(), next.getY()));
							break;
						}
					}

					for (double i = 0; i <= 1.0; i += 0.01)
					{
						Point2D t = Bezier.parabolic2D(vals, i);
						points2.add(new Point((int) t.getX(), (int) t.getY()));
					}

				}

				return points2;
			}

		}, new Color(255, 0, 0));

		ui.addDataSource(routePlanner.getGdPointSource());
	}

	private void addPointBehindRobotForBezier(List<Point2D> vals)
	{
		Vector3D currentPos = new Vector3D(pfX, pfY, 0);
		Vector3D behind = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(pf.getHeading()))
				.applyTo(new Vector3D(0, -50, 0)).add(currentPos);
		vals.add(new Point2D.Double(behind.getX(), behind.getY()));
		vals.add(new Point2D.Double(pfX, pfY));
	}

	private void setupDataSources(MapDrawingWindow ui, final RobotPoseSource pf)
	{

		ui.addStatisticSource(new DataSourceStatistic()
		{

			@Override
			public String getValue()
			{
				return "" + pf.getHeading();
			}

			@Override
			public String getLabel()
			{
				return "deadReconning Heading";
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

		ui.addDataSource(getDeadReconningHeadingMapDataSource());

	}

	@Override
	public void stop()
	{
		stopped = true;

	}

	@Override
	public void go()
	{
		stopped = false;

	}

	@Override
	public void calculateRouteTo(int x, int y, Double heading, RouteOption routeOption)
	{
		reachedDestination = !routePlanner.createRoute(x, y, routeOption);
		targetHeading = heading;
		stuckCounter = 0;

	}

	@Override
	public boolean hasReachedDestination()
	{
		return reachedDestination;
	}

	@Override
	public boolean isStuck()
	{
		return stuckCounter > 100;
	}

	public DataSourceMap getDeadReconningHeadingMapDataSource()
	{
		return new DataSourceMap()
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
				Graphics graphics = image.getGraphics();

				// draw heading line
				graphics.setColor(new Color(128, 128, 0));
				Vector3D line = new Vector3D(60 * scale, 0, 0);
				line = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(pf.getHeading() + 90)).applyTo(line);

				graphics.drawLine((int) pointOriginX, (int) pointOriginY, (int) (pointOriginX + line.getX()),
						(int) (pointOriginY + line.getY()));

			}
		};
	}

}
