package au.com.rsutton.navigation.router.rrt;

import java.util.Random;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;

public class Pose2DWithConstraint implements Pose<Pose2DWithConstraint>
{

	final double x;
	final double y;
	Double theta = null;

	final double maxAngle = 20;
	boolean isReverse;

	Random rand = new Random(1);

	public Pose2DWithConstraint(double x, double y, Double theta, boolean reverse)
	{
		super();
		this.x = x;
		this.y = y;
		this.theta = theta;
		this.isReverse = reverse;

	}

	public double distanceTo(Pose2DWithConstraint otherPose)
	{
		double dx = x - otherPose.x;
		double dy = y - otherPose.y;
		return Math.sqrt((dx * dx) + (dy * dy));

	}

	@Override
	public double calculateCost(Pose2DWithConstraint from)
	{

		// cost is time

		// speed = 1 *(1 - ((180 - abs(angle))/180))

		// cost = distance / speed

		// TODO: calc angle
		// double deltaX = getX() - from.getX();
		// double deltaY = getY() - from.getY();
		// double angle = Math.toDegrees(Math.atan2(deltaY, deltaX));
		// double da = HeadingHelper.getChangeInHeading(angle, theta);

		double distance = distanceTo(from);
		if (isReverse)
		{
			distance *= 1000;
		}
		return distance;// * (Math.max(1, 3 * (Math.abs(da - maxAngle))));
	}

	@Override
	public double getX()
	{
		return x;
	}

	@Override
	public String toString()
	{
		return "Pose2DWithConstraint [x=" + x + ", y=" + y + ", theta=" + theta + "]";
	}

	@Override
	public double getY()
	{
		return y;
	}

	@Override
	public Pose2DWithConstraint moveTowards(Pose2DWithConstraint targetPose)
	{
		if (!canConnect(targetPose))
		{
			throw new RuntimeException("Can't be connected");
		}
		Pose2DWithConstraint newPose = null;
		double dx = targetPose.x - x;
		double dy = targetPose.y - y;

		double scalar = 1.0;
		if (Math.max(Math.abs(dx), Math.abs(dy)) > 1)
		{
			scalar = Math.max(Math.abs(dx), Math.abs(dy));
		}
		if (scalar > 0.01)
		{
			double xAdd = dx / scalar;
			double yAdd = dy / scalar;

			double newTheta = Math.toDegrees(ATan2Cached.atan2(xAdd, yAdd));
			if (targetPose.isReverse)
			{
				// for reverse, flip the angle
				newTheta -= 180;
			}

			// create new node
			newPose = new Pose2DWithConstraint(x + xAdd, y + yAdd, newTheta, targetPose.isReverse);
		}
		return newPose;
	}

	@Override
	public Pose2DWithConstraint getRandomPointInMapSpace(ProbabilityMapIIFc map2, RttPhase rttPhase)
	{

		int dx = map2.getMaxX() - map2.getMinX();
		int dy = map2.getMaxY() - map2.getMinY();

		double x = (rand.nextDouble() * dx) + map2.getMinX();
		double y = (rand.nextDouble() * dy) + map2.getMinY();

		double reverseThreshold = 1.0;
		if (rttPhase == RttPhase.START)
		{
			// NO OP
		} else if (rttPhase == RttPhase.NORMAL)
		{
			reverseThreshold = 0.98;
		} else if (rttPhase == RttPhase.LONG)
		{
			reverseThreshold = 0.90;
		} else if (rttPhase == RttPhase.OPTIMIZE)
		{
			reverseThreshold = 1.0;
		}

		return new Pose2DWithConstraint(x, y, (Math.random() * 360), Math.random() > reverseThreshold);

	}

	@Override
	public boolean canConnect(Pose2DWithConstraint childPose)
	{
		if (!childPose.isReverse && Math.abs(HeadingHelper.getChangeInHeading(childPose.theta, theta)) > 2 * maxAngle)
		{
			// not possible for the angles to match, return early
			return false;
		}
		double dx = childPose.x - x;
		double dy = childPose.y - y;

		double dist = Math.sqrt((dx * dx) + (dy * dy));
		if (dist < 0.250)
		{
			// never less than half a unit.
			return false;
		}

		double localTheta = Math.toDegrees(ATan2Cached.atan2(dx, dy));
		double childTheta = childPose.theta;
		if (childPose.isReverse)
		{
			localTheta -= 180;
			childTheta -= 180;
		}

		double dt = Math.abs(HeadingHelper.getChangeInHeading(localTheta, theta));
		dt = dt / Math.min(1.0, dist);

		if (dt > maxAngle)
		{
			return false;
		}
		return Math.abs(HeadingHelper.getChangeInHeading(localTheta, childTheta)) < maxAngle;
	}

	@Override
	public void updateParent(Pose2DWithConstraint newParentPose)
	{
		// only used by relinkNodes!!

		double deltaX = getX() - newParentPose.getX();
		double deltaY = getY() - newParentPose.getY();
		theta = Math.toDegrees(ATan2Cached.atan2(deltaY, deltaX));
		if (isReverse)
		{
			theta -= 180;
		}

	}

	@Override
	public Pose2DWithConstraint copy()
	{
		return new Pose2DWithConstraint(x, y, theta, isReverse);
	}

	public boolean isReverse()
	{
		return isReverse;
	}

	@Override
	public boolean canBridge(Pose2DWithConstraint pose2)
	{

		Pose2DWithConstraint copy = pose2.copy();
		copy.theta = copy.theta + 180.0;

		return !isReverse && !pose2.isReverse && canConnect(copy);
	}

	@Override
	public Pose2DWithConstraint invertDirection()
	{
		theta = theta + 180;
		return this;
	}

	public Double getTheta()
	{
		return theta;
	}

}
