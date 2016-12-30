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
import au.com.rsutton.mapping.particleFilter.ParticleUpdate;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.navigation.router.ExpansionPoint;
import au.com.rsutton.navigation.router.RouteOption;
import au.com.rsutton.navigation.router.RoutePlanner;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.robot.RobotListener;
import au.com.rsutton.robot.rover.Angle;
import au.com.rsutton.ui.DataSourcePoint;
import au.com.rsutton.ui.DataSourceStatistic;
import au.com.rsutton.ui.MapDrawingWindow;

public class Navigator implements Runnable, NavigatorControl
{

	private ProbabilityMap map;
	private ParticleFilterIfc pf;
	private RoutePlanner routePlanner;
	private RobotInterface robot;
	private MapDrawingWindow ui;
	private ScheduledExecutorService pool;
	private volatile boolean stopped = true;
	private ObsticleAvoidance obsticleAvoidance;

	public Navigator(ProbabilityMap map, ParticleFilterIfc pf, RobotInterface robot)
	{
		ui = new MapDrawingWindow();
		this.map = map;
		ui.addDataSource(map, new Color(255, 255, 255));
		this.pf = pf;
		setupDataSources(ui, pf);

		// FeatureExtractionTestFullWold dl4j = new
		// FeatureExtractionTestFullWold();
		// dl4j.train();
		// ui.addDataSource(dl4j.getHeadingMapDataSource(pf, robot));

		routePlanner = new RoutePlanner(map);
		this.robot = robot;

		obsticleAvoidance = new ObsticleAvoidance(robot);
		ui.addDataSource(obsticleAvoidance.getHeadingMapDataSource(pf, robot));

		setupRoutePlanner();

		setupRobotListener();

		pool = Executors.newScheduledThreadPool(1);

		pool.scheduleWithFixedDelay(this, 500, 500, TimeUnit.MILLISECONDS);

	}

	int pfX = 0;
	int pfY = 0;
	Integer initialX;
	Integer initialY;
	double lastAngle;
	private boolean reachedDestination = false;
	final AtomicDouble currentDeadReconingHeading = new AtomicDouble();

	double speed = 0;

	@Override
	public void run()
	{
		try
		{
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
			pf.setParticleCount(Math.max(500, (int) (7 * std)));

			if (std < 30)
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
					if (Math.abs(da) > 90)
					{
						// turning more than 90 degrees, stop while we do it.
						System.out.println("Setting speed to 0");
						speed *= 0.0;
					}
					speed = setHeadingWithObsticleAvoidance(HeadingHelper.normalizeHeading(da), speed);
					robot.setSpeed(new Speed(new Distance(speed, DistanceUnit.CM), Time.perSecond()));

				} else
				{
					// we have arrived at our target location
					speed = 0;
					if (Math.abs(HeadingHelper.getChangeInHeading(lastAngle, targetHeading)) > 5)
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
						try
						{
							Thread.sleep(500);
						} catch (InterruptedException e)
						{
							e.printStackTrace();
						}
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
			// robot.freeze(true);
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

	private float compassHeading;
	private double targetHeading;

	private void setupRobotListener()
	{
		robot.addMessageListener(new RobotListener()
		{

			Double lastx = null;
			Double lasty = null;
			private Angle lastheading;

			// MovingLidarObservationMultiBuffer buffer = new
			// MovingLidarObservationMultiBuffer(2);

			@Override
			public void observed(RobotLocation robotLocation)
			{
				compassHeading = robotLocation.getCompassHeading().getHeading();
				currentDeadReconingHeading.set(robotLocation.getDeadReaconingHeading().getDegrees());

				// ParticleFilterObservationSet bufferedObservations =
				// updateBuffer(robotLocation);
				pf.addObservation(map, robotLocation, -90d);

				if (lastx != null)
				{
					pf.moveParticles(new ParticleUpdate()
					{

						@Override
						public double getDeltaHeading()
						{

							return HeadingHelper.getChangeInHeading(
									robotLocation.getDeadReaconingHeading().getDegrees(), lastheading.getDegrees());
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

				// pf.dumpTextWorld(KitchenMapBuilder.buildKitchenMap());

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
				return "" + compassHeading;
			}

			@Override
			public String getLabel()
			{
				return "compass Heading";
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
				return "" + pf.getBestRating();
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
	public void calculateRouteTo(int x, int y, double heading)
	{
		routePlanner.createRoute(x, y, RouteOption.ROUTE_THROUGH_CLEAR_SPACE_ONLY);
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

}
