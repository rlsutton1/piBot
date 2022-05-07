package au.com.rsutton.navigation.router;

import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.robot.RobotPoseSourceTimeTraveling.RobotPoseInstant;
import au.com.rsutton.units.Distance;

public class ProxyPlanner implements RoutePlannerFinalStage
{
	// TODO: we will wrapper RoutePlanner3D
	@Override
	public void plan(RobotPoseInstant findInstant, RoutePlanner newLocalPlanner, ProbabilityMapIIFc world)

	{
		// TODO Auto-generated method stub

	}

	@Override
	public double getTurnRadius()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getDirection()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAbsoluteTotalDistance(Distance absoluteTotalDistance)
	{
		// TODO Auto-generated method stub

	}

}
