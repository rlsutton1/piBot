package au.com.rsutton.navigation.router.md;

public interface PlannerIfc
{

	public void plan(int x, int y, RPAngle angle, MoveTemplate[] moveTemplates);

	public MoveTemplate getNextMove(int x, int y, RPAngle angle);
}
