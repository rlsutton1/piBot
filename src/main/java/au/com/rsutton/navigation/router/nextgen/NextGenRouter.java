package au.com.rsutton.navigation.router.nextgen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.rsutton.hazelcast.LidarScan;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.navigation.router.ExpansionPoint;
import au.com.rsutton.navigation.router.RouteOption;
import au.com.rsutton.navigation.router.RoutePlanner;
import au.com.rsutton.navigation.router.RoutePlannerImpl;
import au.com.rsutton.navigation.router.RoutePlannerRRT;
import au.com.rsutton.navigation.router.rrt.Pose2DWithConstraint;
import au.com.rsutton.navigation.router.rrt.RrtNode;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.robot.RobotLocationDeltaListener;
import au.com.rsutton.robot.RobotPoseSourceTimeTraveling;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.AngleUnits;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;
import au.com.rsutton.units.Pose;

public class NextGenRouter implements PathPlannerAndFollowerIfc
{
	ProbabilityMapIIFc map;
	RobotInterface robot;
	RobotPoseSourceTimeTraveling pf;

	RoutePlanner baseAStarPlanner;
	private RoutePlannerRRT rrtPlanner;
	private RrtNode<Pose2DWithConstraint> path;

	Distance pathStartDistance;
	Distance absoluteTotalDistance;
	private boolean hasPlannedRoute = false;

	Logger logger = LogManager.getLogger();

	public NextGenRouter(ProbabilityMapIIFc map, RobotInterface robot, RobotPoseSourceTimeTraveling pf)
	{
		this.map = map;
		this.robot = robot;
		this.pf = pf;

		baseAStarPlanner = new RoutePlannerImpl(map);
		rrtPlanner = new RoutePlannerRRT();

		robot.addMessageListener(new RobotLocationDeltaListener()
		{

			@Override
			public void onMessage(LidarScan scan)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void onMessage(Angle deltaHeading, Distance deltaDistance, boolean bump,
					Distance absoluteTotalDistance)
			{
				logger.error("Setting total distance to " + absoluteTotalDistance);
				NextGenRouter.this.absoluteTotalDistance = absoluteTotalDistance;
			}
		});
	}

	@Override
	public boolean planPath(Pose toPose)
	{

		hasPlannedRoute = false;
		if (baseAStarPlanner.createRoute((int) toPose.getX(), (int) toPose.getY(),
				RouteOption.ROUTE_THROUGH_CLEAR_SPACE_ONLY))
		{

			path = rrtPlanner.plan(pf.findInstant(System.currentTimeMillis()), baseAStarPlanner, map);
			if (path != null)
			{
				hasPlannedRoute = true;
				pathStartDistance = absoluteTotalDistance;
			}

		}

		return hasPlannedRoute;
	}

	@Override
	public DirectionAndAngle getNextStep()
	{
		double distance = absoluteTotalDistance.convert(DistanceUnit.CM) - pathStartDistance.convert(DistanceUnit.CM);

		logger.error("Total distance is " + absoluteTotalDistance);
		logger.error("path start distance is " + pathStartDistance);
		logger.error("distance is " + distance);
		logger.error("");

		RrtNode<Pose2DWithConstraint> node = path;
		double traveled = 0;
		int ctr = 0;
		while (traveled < distance)
		{
			ctr++;
			RrtNode<Pose2DWithConstraint> parent = node.getParent();
			if (parent == null)
			{
				logger.error("ctr=" + ctr + " traveled=" + traveled);
				return null;
			}

			traveled += (parent.getPose().distanceTo(node.getPose()) * map.getBlockSize());
			node = parent;
		}

		Pose2DWithConstraint pose = node.getPose();
		Angle angle = new Angle(pose.getTheta(), AngleUnits.DEGREES);

		return new DirectionAndAngle(pose.isReverse(), angle);
	}

	@Override
	public ExpansionPoint getLocationOfStepAt(Distance distanceForward)
	{
		RrtNode<Pose2DWithConstraint> node = path;
		double traveled = 0;
		while (traveled < distanceForward.convert(DistanceUnit.CM))
		{

			RrtNode<Pose2DWithConstraint> parent = node.getParent();
			if (parent == null)
			{

				break;
			}

			traveled += (parent.getPose().distanceTo(node.getPose()) * map.getBlockSize());
			node = parent;
		}

		return new ExpansionPoint((int) convertX(node.getX()), (int) convertY(node.getY()), 0, null);
	}

	private double convertX(double x)
	{
		return (x - rrtPlanner.getXoffset()) * map.getBlockSize();
	}

	private double convertY(double y)
	{
		return (y - rrtPlanner.getYoffset()) * map.getBlockSize();
	}

	@Override
	public boolean hasPlannedRoute()
	{
		return hasPlannedRoute;
	}

	@Override
	public Distance getDistanceToTarget()
	{
		int x = (int) pf.getXyPosition().getX().convert(DistanceUnit.CM);
		int y = (int) pf.getXyPosition().getY().convert(DistanceUnit.CM);
		return new Distance(baseAStarPlanner.getDistanceToTarget(x, y), DistanceUnit.CM);
	}

	public class DirectionAndAngle
	{
		boolean isReverse;
		Angle angle;

		DirectionAndAngle(boolean isReverse, Angle angle)
		{
			this.isReverse = isReverse;
			this.angle = angle;
		}

		public boolean isReverse()
		{
			return isReverse;
		}

		public Angle getAngle()
		{
			return angle;
		}
	}

}
