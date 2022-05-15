package au.com.rsutton.navigation.router.md;

import au.com.rsutton.navigation.router.md.RoutePlanner3D.Angle;
import au.com.rsutton.navigation.router.md.RoutePlanner3D.MoveTemplate;

public interface PlannerIfc
{

	public void plan(int x, int y, Angle angle, MoveTemplate[] moveTemplates);

	public MoveTemplate getNextMove(int x, int y, Angle angle);
}
