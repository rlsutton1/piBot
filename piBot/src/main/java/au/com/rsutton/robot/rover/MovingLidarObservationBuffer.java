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

	void addLidarObservation(RobotLocation data)
	{

		double heading = data.getHeading().getRadians();
		Rotation frameRotation = new Rotation(RotationOrder.XYZ, 0, 0, heading);

		// due to a horrible bug in old code we have to invert the X axis
		// must replace all old code with Vector3D
		double xCm = -data.getX().convert(DistanceUnit.CM);
		double yCm = data.getY().convert(DistanceUnit.CM);
		Vector3D frameTranslation = new Vector3D(xCm, yCm, 0);

		for (LidarObservation observation : data.getObservations())
		{

			Vector3D resolvedObservation = frameRotation.applyTo(observation.getVector()).add(frameTranslation);

			observations.add(new LidarObservation(resolvedObservation, observation.isStartOfScan()));
			for (LidarObservation obs : observations)
			{
				if (obs.getVector().equals(resolvedObservation))
				{
					System.out.println("Error Error, duplicate observation in buffer " + resolvedObservation
							+ " buffer size " + observations.size());
				}
			}

		}

	}

	List<LidarObservation> getTranslatedObservations(RobotLocation data)
	{
		double heading = data.getHeading().getRadians();
		Rotation frameRotation = new Rotation(RotationOrder.XYZ, 0, 0, heading);

		// due to a horrible bug in old code we have to invert the X axis
		// must replace all old code with Vector3D
		double xCm = -data.getX().convert(DistanceUnit.CM);
		double yCm = data.getY().convert(DistanceUnit.CM);
		Vector3D frameTranslation = new Vector3D(xCm, yCm, 0);

		return getTranslatedObservations(frameRotation, frameTranslation);
	}

	List<LidarObservation> getTranslatedObservations(Rotation frameRotation, Vector3D frameTranslation)
	{
		List<LidarObservation> translatedObservations = new LinkedList<>();
		for (LidarObservation observation : observations)
		{
			Vector3D worldFrame = observation.getVector();

			translatedObservations.add(new LidarObservation(frameRotation.applyInverseTo(worldFrame
					.subtract(frameTranslation)), false));
		}
		return translatedObservations;
	}

}
