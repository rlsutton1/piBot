package au.com.rsutton.navigation.router;

import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.robot.RobotPoseSourceTimeTraveling.RobotPoseInstant;
import au.com.rsutton.units.Distance;

public interface RoutePlannerFinalStage
{

	void plan(RobotPoseInstant findInstant, RoutePlanner newLocalPlanner, ProbabilityMapIIFc world);

	double getTurnRadius();

	int getDirection();

	void setAbsoluteTotalDistance(Distance absoluteTotalDistance);

}
