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

import au.com.rsutton.depthcamera.PointCloudUI;
import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.hazelcast.DataLogValue;
import au.com.rsutton.mapping.particleFilter.RobotPoseSource;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.navigation.router.ExpansionPoint;
import au.com.rsutton.navigation.router.RouteOption;
import au.com.rsutton.navigation.router.RoutePlanner;
import au.com.rsutton.navigation.router.RoutePlannerLastMeter;
import au.com.rsutton.robot.RobotInterface;
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

	private int MAX_SPEED = 30;

	Logger logger = LogManager.getLogger();

	private RobotPoseSource pf;
	private RoutePlanner routePlanner;
	private RobotInterface robot;
	private MapDrawingWindow ui;
	private ScheduledExecutorService pool;
	private volatile boolean stopped = true;
	private ObsticleAvoidance obsticleAvoidance;
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
	private RobotPoseSource slam;

	// private Double initialHeading = null;

	public Navigator(ProbabilityMapIIFc map2, RobotPoseSource pf, RobotInterface robot, int maxSpeed)
	{
		this.MAX_SPEED = maxSpeed;
		ui = new MapDrawingWindow("Navigator", 600, 0, 250);
		ui.addDataSource(map2, new Color(255, 255, 255));

		// add data source from depth camera
		ui.addDataSource(new PointCloudUI(pf));

		this.pf = pf;
		setupDataSources(ui, pf);

		// FeatureExtractor extractor = new FeatureExtractorSpike(null);
		// ui.addDataSource(extractor.getHeadingMapDataSource(pf, robot));
		//
		// FeatureExtractor extractor2 = new FeatureExtractorCorner(null);
		// ui.addDataSource(extractor2.getHeadingMapDataSource(pf, robot));

		// FeatureExtractionTestFullWold dl4j = new
		// FeatureExtractionTestFullWold();
		// dl4j.train();
		// ui.addDataSource(dl4j.getHeadingMapDataSource(pf, robot));

		routePlanner = new RoutePlannerLastMeter(map2, robot, pf);
		this.robot = robot;

		obsticleAvoidance = new ObsticleAvoidance(robot, pf);
		ui.addDataSource(obsticleAvoidance.getHeadingMapDataSource(pf, robot));

		setupRoutePlanner(map2);

		setupRobotListener();

		pool = Executors.newScheduledThreadPool(1);

		pool.scheduleWithFixedDelay(this, 500, 500, TimeUnit.MILLISECONDS);

	}

	public RobotPoseSource getSlam()
	{
		return slam;
	}

	@Override
	public void run()
	{
		try
		{

			if (stopped)
			{
				robot.freeze(true);
				robot.publishUpdate();
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

				int tx = pfX;
				int ty = pfY;
				if (routePlanner.hasPlannedRoute())
				{
					// get a point on the route 25 steps from where we are, we
					// look ahead (25 steps) so our heading remains reasonably
					// stable
					next = routePlanner.getRouteForLocation(pfX, pfY);

					int i = 0;
					for (; i < 300; i++)
					{
						next = routePlanner.getRouteForLocation(next.getX(), next.getY());
						if (i == 0 || i % 50 == 0)
						{
							vals.add(new Point2D.Double(next.getX(), next.getY()));
						}
					}
					vals.add(new Point2D.Double(next.getX(), next.getY()));

					Point2D t = Bezier.parabolic2D(vals, Math.min(1.0, 30.0 / i));
					tx = (int) t.getX();
					ty = (int) t.getY();

				}
				double distanceToTarget = routePlanner.getDistanceToTarget(pfX, pfY);

				// slow as we approach the target
				speed = Math.min(Math.max(3, distanceToTarget), speed);

				double dx = tx - pfX;
				double dy = ty - pfY;

				double da = 5;
				if (Math.abs(dx) < 1 && Math.abs(dy) < 1)
				{
					speed = 0;
					robot.freeze(true);
					robot.publishUpdate();
					Thread.sleep(500);
				} else

				if (distanceToTarget > 20)
				{
					// follow the route to the target

					Vector3D delta = new Vector3D(dx, dy, 0);
					double angle = Math.toDegrees(Math.atan2(delta.getY(), delta.getX())) - 90;

					da = HeadingHelper.getChangeInHeading(angle, lastAngle);
					speed *= ((180 - Math.abs(da)) / 180.0);
					if (Math.abs(da) > 35)
					{
						// turning more than 35 degrees, stop while we do it.
						logger.debug("Setting speed to 0");
						speed = 10;
					}
					speed = setHeadingWithObsticleAvoidance(HeadingHelper.normalizeHeading(da), speed);
					robot.setSpeed(new Speed(new Distance(speed, DistanceUnit.CM), Time.perSecond()));

				} else
				{
					// we have arrived at our target location
					speed = 0;
					if (targetHeading != null
							&& Math.abs(HeadingHelper.getChangeInHeading(lastAngle, targetHeading)) > 5)
					{
						// turn on the spot to set our heading
						da = -HeadingHelper.getChangeInHeading(targetHeading, lastAngle);
						robot.setSpeed(new Speed(new Distance(5, DistanceUnit.CM), Time.perSecond()));
						robot.turn(da);

					} else
					{
						// were done, stop and shutdown
						stopped = true;
						speed = 0;
						reachedDestination = true;
						robot.freeze(true);
						robot.publishUpdate();
						Thread.sleep(500);
					}
				}

			} else
			{
				// particle filter isn't localised, just turn on the spot
				speed = setHeadingWithObsticleAvoidance(HeadingHelper.normalizeHeading(0), 10);
				robot.turn(0);
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

	double setHeadingWithObsticleAvoidance(double desiredHeading, double desiredSpeed)
	{

		CourseCorrection corrected = obsticleAvoidance.getCorrectedHeading(desiredHeading, desiredSpeed);

		Double correctedRelativeHeading = corrected.getCorrectedRelativeHeading();

		new DataLogValue("Heading : corrected", "" + desiredHeading + " : " + correctedRelativeHeading).publish();
		robot.turn(360 - correctedRelativeHeading);

		return corrected.getSpeed();

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

				List<Point2D> vals = new LinkedList<>();

				List<Point> points2 = new LinkedList<>();
				List<Point> points = new LinkedList<>();
				if (routePlanner.hasPlannedRoute())
				{

					for (int i = 0; i < 3000; i++)
					{
						ExpansionPoint next = routePlanner.getRouteForLocation((int) x, (int) y);
						points.add(new Point(next.getX(), next.getY()));

						if (i % 20 == 0)
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

					for (double i = 0.0; i <= 1.0; i += 0.01)
					{
						Point2D t = Bezier.parabolic2D(vals, i);
						points2.add(new Point((int) t.getX(), (int) t.getY()));
					}

				}

				return points2;
			}

		}, new Color(255, 0, 0));
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
					List<Point2D> vals = new LinkedList<>();

					for (int i = 0; i < 3000; i++)
					{
						ExpansionPoint next = ((RoutePlannerLastMeter) routePlanner).getMasterRouteForLocation((int) x,
								(int) y);
						if (i % 50 == 0 || vals.size() == 0)
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
					for (double i = 0.0; i <= 1.0; i += 0.01)
					{
						Point2D t = Bezier.parabolic2D(vals, i);
						points.add(new Point((int) t.getX(), (int) t.getY()));
					}
				}

				return points;
			}

		}, new Color(0, 255, 0));
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

	}

	@Override
	public boolean hasReachedDestination()
	{
		return reachedDestination;
	}

	@Override
	public boolean isStuck()
	{
		return false;
	}

	@Override
	public boolean isStopped()
	{
		return stopped;
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

	@Override
	public ExpansionPoint getRouteForLocation(int x, int y)
	{
		return routePlanner.getRouteForLocation(x, y);
	}

}
