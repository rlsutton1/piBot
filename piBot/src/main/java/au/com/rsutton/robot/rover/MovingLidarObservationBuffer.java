package au.com.rsutton.robot.rover;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.hazelcast.RobotLocation;

public class MovingLidarObservationBuffer
{
	List<LidarObservation> observations = new LinkedList<>();
	Vector3D frameTranslation = null;
	Rotation frameRotation = null;

	void addLidarObservation(RobotLocation data)
	{
		data.getX();
		data.getY();

		data.getHeading();

		Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, data.getHeading().getRadians());
		Vector3D translation = new Vector3D(data.getX().convert(DistanceUnit.CM), data.getY().convert(DistanceUnit.CM),
				0);

		if (observations.isEmpty())
		{
			frameRotation = rotation;
			frameTranslation = translation;
		}

		for (LidarObservation observation : data.getObservations())
		{
			double angle = rotation.getAngles(RotationOrder.XYZ)[2];
			double frameAngle = frameRotation.getAngles(RotationOrder.XYZ)[2];
			double deltaAngle = frameAngle - angle;

			Rotation deltaRotation = new Rotation(RotationOrder.XYZ, 0, 0, deltaAngle);

			Vector3D deltaTranslation = frameTranslation.subtract(translation);

			Vector3D resolvedObservation = deltaRotation.applyInverseTo(observation.getVector().subtract(
					deltaTranslation));

			observations.add(new LidarObservation(resolvedObservation,false));

		}

	}
	
	List<LidarObservation> getTranslatedObservations(Rotation rotation, Vector3D translation)
	{
		List<LidarObservation> translatedObservations = new LinkedList<>();
		for (LidarObservation observation :observations)
		{
			Vector3D worldFrame = frameRotation.applyInverseTo(observation.getVector().add(frameTranslation));
			
			translatedObservations.add(new LidarObservation(rotation.applyInverseTo(worldFrame).subtract(translation),false));
		}
		return translatedObservations;
	}

}
