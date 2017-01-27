package au.com.rsutton.navigation;

import java.awt.Color;
import java.awt.Point;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.util.concurrent.AtomicDouble;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.mapping.particleFilter.ParticleFilterIfc;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.navigation.router.ExpansionPoint;
import au.com.rsutton.navigation.router.RouteOption;
import au.com.rsutton.navigation.router.RoutePlanner;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.robot.RobotListener;
import au.com.rsutton.ui.DataSourcePoint;
import au.com.rsutton.ui.DataSourceStatistic;
import au.com.rsutton.ui.MapDrawingWindow;

public class Navigator implements Runnable, NavigatorControl
{

	private ProbabilityMapIIFc map;
	private ParticleFilterIfc pf;
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
	final AtomicDouble currentDeadReconingHeading = new AtomicDouble();

	/**
	 * if target heading is null, any orientation will do
	 */
	private Double targetHeading;

	double speed = 0;

	public Navigator(ProbabilityMapIIFc map2, ParticleFilterIfc pf, RobotInterface robot)
	{
		ui = new MapDrawingWindow();
		this.map = map2;
		ui.addDataSource(map2, new Color(255, 255, 255));
		this.pf = pf;
		setupDataSources(ui, pf);

		// FeatureExtractionTestFullWold dl4j = new
		// FeatureExtractionTestFullWold();
		// dl4j.train();
		// ui.addDataSource(dl4j.getHeadingMapDataSource(pf, robot));

		routePlanner = new RoutePlanner(map2);
		this.robot = robot;

		obsticleAvoidance = new ObsticleAvoidance(robot);
		ui.addDataSource(obsticleAvoidance.getHeadingMapDataSource(pf, robot));

		setupRoutePlanner();

		setupRobotListener();

		pool = Executors.newScheduledThreadPool(1);

		pool.scheduleWithFixedDelay(this, 500, 500, TimeUnit.MILLISECONDS);

	}

	volatile boolean isSuspended = false;

	@Override
	public void run()
	{
		try
		{
			if (isSuspended)
			{
				return;
			}

			robot.freeze(false);

			if (stopped)
			{
				robot.freeze(true);
				robot.publishUpdate();
				return;
			}
			speed = 15;

			double std = pf.getStdDev();
			// adjust the number of particles in the particle filter based on
			// how well localised it is
			pf.setParticleCount(Math.max(50, (int) (7 * std)));

			if (std < 40)
			{
				// the partical filter is sufficently localized
				Vector3D ap = pf.dumpAveragePosition();
				pfX = (int) ap.getX();
				pfY = (int) ap.getY();

				if (initialX == null)
				{
					// record our starting position, once the particle filter is
					// localized the first time
					initialX = pfX;
					initialY = pfY;
				}
				lastAngle = pf.getAverageHeading();

				ExpansionPoint next = new ExpansionPoint(pfX, pfY);
				if (routePlanner.hasPlannedRoute())
				{
					// get a point on the route 25 steps from where we are, we
					// look ahead (25 steps) so our heading remains reasonably
					// stable
					next = routePlanner.getRouteForLocation(pfX, pfY);

					for (int i = 0; i < 25; i++)
						next = routePlanner.getRouteForLocation(next.getX(), next.getY());
				}
				if (routePlanner.getDistanceToTarget(pfX, pfY) < 20)
				{
					// slow down we've almost reached our goal
					speed = 5;
				}

				double dx = next.getX() - pfX;
				double dy = next.getY() - pfY;
				System.out.println(next + " " + dx + " " + dy);

				double da = 5;
				if (Math.abs(dx) > 5 || Math.abs(dy) > 5)
				{
					// follow the route to the target

					Vector3D delta = new Vector3D(dx, dy, 0);
					double angle = Math.toDegrees(Math.atan2(delta.getY(), delta.getX())) - 90;

					da = HeadingHelper.getChangeInHeading(angle, lastAngle);
					speed *= ((180 - Math.abs(da)) / 180.0);
					if (Math.abs(da) > 35)
					{
						// turning more than 35 degrees, stop while we do it.
						System.out.println("Setting speed to 0");
						speed *= 0.0;
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
						da = HeadingHelper.getChangeInHeading(targetHeading, lastAngle);
						robot.setSpeed(new Speed(new Distance(0, DistanceUnit.CM), Time.perSecond()));
						robot.setHeading(da + currentDeadReconingHeading.get());

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
				// particle filter isn't localised, just wander trusting our
				// obsticalAvoidance to keep us from crashing
				speed = setHeadingWithObsticleAvoidance(HeadingHelper.normalizeHeading(0), 10);
				robot.setSpeed(new Speed(new Distance(speed, DistanceUnit.CM), Time.perSecond()));

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

		robot.setHeading(HeadingHelper
				.normalizeHeading(corrected.getCorrectedRelativeHeading() + currentDeadReconingHeading.get()));

		return corrected.getSpeed();

	}

	private void setupRobotListener()
	{
		robot.addMessageListener(new RobotListener()
		{

			@Override
			public void observed(RobotLocation robotLocation)
			{
				currentDeadReconingHeading.set(robotLocation.getDeadReaconingHeading().getDegrees());

			}

		});
	}

	private void setupRoutePlanner()
	{

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
				if (routePlanner.hasPlannedRoute())
				{

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
				}

				return points;
			}

		}, new Color(255, 255, 0));
	}

	private void setupDataSources(MapDrawingWindow ui, final ParticleFilterIfc pf)
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
				return "" + currentDeadReconingHeading;
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
				return "" + pf.getBestScanMatchScore() + " " + pf.getBestRawScore();
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
		routePlanner.createRoute(x, y, routeOption);
		targetHeading = heading;
		reachedDestination = false;

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

	@Override
	public void suspend()
	{
		isSuspended = true;

	}

	@Override
	public void resume()
	{
		isSuspended = false;
	}

}
