package au.com.rsutton.navigation.router.nextgen;

import au.com.rsutton.navigation.router.ExpansionPoint;
import au.com.rsutton.navigation.router.nextgen.NextGenRouter.DirectionAndAngle;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.Pose;

/**
 * The expected flow is that planPath will be called.
 * 
 * Then getNextStep will be called repeatedly until the destination is reached.
 * 
 * 
 * @author rsutton
 *
 */
public interface PathPlannerAndFollowerIfc
{

	/**
	 * blocking, this method may take a long time to execute while it plans a
	 * path
	 * 
	 * It plans a path from the current pose to the toPose
	 * 
	 * It is assumed that the implementer will have access to the current pose,
	 * assumably by having a reference to the particle filter.
	 * 
	 * 
	 * @param toPose
	 * @param map
	 */
	boolean planPath(Pose toPose);

	/**
	 * This method will check that the currentPose is close enough to the
	 * expected Pose given the current path and the distance travelled.
	 * 
	 * It will also ensure that following the path will not intersect any
	 * Obstacles in the provided updated map
	 * 
	 * @param currentPose
	 * @param absoluteDistanceTravelled
	 * @param map
	 */
	DirectionAndAngle getNextStep();

	/**
	 * use for rendering the intended path in the UI.
	 * 
	 * @param distanceForward
	 * @return
	 */
	ExpansionPoint getLocationOfStepAt(Distance distanceForward);

	boolean hasPlannedRoute();

	Distance getDistanceToTarget();

}
