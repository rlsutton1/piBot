package au.com.rsutton.navigation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
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
import au.com.rsutton.mapping.particleFilter.Pose;
import au.com.rsutton.mapping.particleFilter.RobotPoseSource;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.navigation.router.ExpansionPoint;
import au.com.rsutton.navigation.router.RoutePlannerRRT;
import au.com.rsutton.navigation.router.nextgen.NextGenRouter;
import au.com.rsutton.navigation.router.nextgen.NextGenRouter.DirectionAndAngle;
import au.com.rsutton.navigation.router.nextgen.PathPlannerAndFollowerIfc;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.robot.roomba.Roomba630;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.ui.DataSourcePoint;
import au.com.rsutton.ui.DataSourceStatistic;
import au.com.rsutton.ui.MapDrawingWindow;
import au.com.rsutton.units.AngleUnits;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;
import au.com.rsutton.units.Speed;

public class Navigator implements Runnable, NavigatorControl
{

	private final int MAX_SPEED;

	Logger logger = LogManager.getLogger();

	private RobotPoseSource pf;
	private PathPlannerAndFollowerIfc routePlanner;
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

	Speed speed = Speed.ZERO;

	private volatile int stuckCounter = 0;

	// private Double initialHeading = null;

	public Navigator(ProbabilityMapIIFc map, RobotPoseSourceTimeTraveling pf, RobotInterface robot, int maxSpeed)
	{
		this.MAX_SPEED = maxSpeed;
		ui = new MapDrawingWindow("Navigator", 600, 0, 250, true);
		ui.addDataSource(map, new Color(255, 255, 255));

		this.pf = pf;
		setupDataSources(ui, pf);

		routePlanner = new NextGenRouter(map, robot, pf);

		// add data source
		// ui.addDataSource((DataSourceMap) routePlanner);

		this.robot = robot;

		setupRoutePlanner(map);

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
			speed = Speed.cmPerSec(50);

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

				DirectionAndAngle directionAndAngle = routePlanner.getNextStep();
				Distance distanceToTarget = routePlanner.getDistanceToTarget();

				// slow as we approach the target
				speed = Speed.cmPerSec(
						Math.min(Math.max(3, distanceToTarget.convert(DistanceUnit.CM)), speed.getCmPerSec()));

				if (distanceToTarget.convert(DistanceUnit.CM) > 20)
				{
					// follow the route to the target
					if (directionAndAngle.isReverse())
					{
						speed = Speed.cmPerSec(-5);
					}

					logger.warn("Calculating radius of arc to send to roomba");
					int radius = Roomba630.STRAIGHT;
					double angleDifference = directionAndAngle.getAngle().difference(pf.getHeading(),
							AngleUnits.DEGREES);
					if (Math.abs(angleDifference) > 0)
					{
						radius = (int) RoutePlannerRRT.getRadiusOfArc(angleDifference, 1);
					}
					speed = setHeadingWithObsticleAvoidance(radius, speed);
					robot.setSpeed(speed);
					System.out.println("1 Set speed to " + speed);
				} else
				{
					// we have arrived at our target location
					System.out.println("3 Set speed to 0");
					// were done, stop and shutdown
					stopped = true;
					speed = Speed.ZERO;
					reachedDestination = true;
					robot.freeze(true);
					Thread.sleep(500);
				}

			} else
			{
				// particle filter isn't localised, just turn on the spot
				speed = setHeadingWithObsticleAvoidance(HeadingHelper.normalizeHeading(0), Speed.cmPerSec(10));
				robot.setStraight("Nav - unlocal");
				robot.setSpeed(Speed.ZERO);

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

	Speed setHeadingWithObsticleAvoidance(double desiredTurnRadius, Speed speed2)
	{
		double setRadis = desiredTurnRadius;

		if (Math.abs(desiredTurnRadius) >= Roomba630.STRAIGHT || desiredTurnRadius == 0)
		{

			robot.setStraight("NAV set Head");
			new DataLogValue("Desired Turn Radius", "Straight NAV", DataLogLevel.INFO).publish();
			return speed2;
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

		return Speed.cmPerSec(Math.min(speed2.getCmPerSec(), maxSpeed));
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
						ExpansionPoint next = routePlanner.getLocationOfStepAt(new Distance(i, DistanceUnit.CM));

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
				List<Point> points = new LinkedList<>();
				if (routePlanner.hasPlannedRoute())
				{

					for (int i = 0; i < 3000; i++)
					{
						ExpansionPoint next = routePlanner.getLocationOfStepAt(new Distance(i, DistanceUnit.CM));
						points.add(new Point(next.getX(), next.getY()));
					}
				}
				return points;
			}

		}, new Color(255, 0, 0));

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
	public void calculateRouteTo(Pose pose)
	{
		reachedDestination = !routePlanner.planPath(pose);
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
