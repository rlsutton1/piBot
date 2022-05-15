package au.com.rsutton.navigation.router.md;

public interface PlannerIfc
{

	public void plan(RpPose target, MoveTemplate[] moveTemplates);

	public MoveTemplate getNextMove(RpPose target);
}
