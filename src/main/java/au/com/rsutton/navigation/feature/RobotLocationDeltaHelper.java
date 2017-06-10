package au.com.rsutton.navigation.feature;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.robot.rover.Angle;

public class RobotLocationDeltaHelper
{

	/**
	 * applies the deltas to the x and y supplied with reference to the supplied
	 * heading
	 * 
	 * @param deltaHeading
	 * @param deltaDistance
	 * @param currentHeading
	 * @param currentX
	 * @param y
	 * @return
	 */
	public static DistanceXY applyDelta(Angle deltaHeading, Distance deltaDistance, Angle currentHeading,
			Distance currentX, Distance currentY)
	{
		// TODO: this is wrong because it ignores the fact that the distance
		// travelled
		// was along an arc rather than a straight line

		double directionOfTravel = currentHeading.getDegrees() + deltaHeading.getDegrees();

		Vector3D travel = new Vector3D(0, deltaDistance.convert(DistanceUnit.MM), 0);
		Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, directionOfTravel);

		Vector3D location = new Vector3D(currentX.convert(DistanceUnit.MM), currentY.convert(DistanceUnit.MM), 0);

		Vector3D result = location.add(rotation.applyTo(travel));

		return new DistanceXY(result.getX(), result.getY(), DistanceUnit.MM);

	}
}
