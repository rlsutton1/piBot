package au.com.rsutton.navigation.router;

import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.navigation.router.md.MoveTemplate;

public interface RoutePlanner
{

	public void createPlannerForMap(ProbabilityMapIIFc probabilityMap);

	public void plan(int x, int y, double heading);

	MoveTemplate getNextMove(int pfX, int pfY, double heading);

	public boolean hasPlannedRoute();

}