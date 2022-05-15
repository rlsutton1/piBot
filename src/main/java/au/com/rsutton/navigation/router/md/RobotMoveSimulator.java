package au.com.rsutton.navigation.router.md;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class RobotMoveSimulator
{
	RpPose internalPose;

	public RobotMoveSimulator(RpPose pose)
	{
		internalPose = pose;
	}

	public void performMove(MoveTemplate move)
	{
		RPAngle newAngle = new RPAngle(internalPose.getAngle().getDegrees() - move.angleDelta.getDegrees());
		Vector3D uv = RoutePlanner3D.getUnitVector(newAngle);
		Vector3D location = new Vector3D(internalPose.getX(), internalPose.getY(), 0);
		if (move.forward)
		{
			location = location.add(uv);
		} else
		{
			location = location.subtract(uv);
		}

		internalPose = new RpPose(location.getX(), location.getY(), newAngle);
	}

	public RpPose getPose()
	{
		return internalPose;
	}
}