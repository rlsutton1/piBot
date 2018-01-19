package au.com.rsutton.robot.lidar;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.rsutton.mapping.particleFilter.RobotPoseSource;
import au.com.rsutton.mapping.particleFilter.ScanObservation;
import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.robot.rover.LogLevelHelper;
import au.com.rsutton.units.DistanceUnit;

public class MovingLidarObservationBuffer
{
	List<ObservationSet> observations = new LinkedList<>();

	private RobotPoseSource poseSource;

	Logger logger = LogManager.getLogger();

	class ObservationSet
	{
		double heading;
		DistanceXY position;
		List<ScanObservation> observations = new LinkedList<>();

	}

	public MovingLidarObservationBuffer(RobotPoseSource poseSource)
	{
		this.poseSource = poseSource;

		LogLevelHelper.setLevel(logger, Level.ERROR);

	}

	public void addLidarObservation(List<ScanObservation> data)
	{

		ObservationSet set = new ObservationSet();
		set.heading = poseSource.getHeading();
		set.position = poseSource.getXyPosition();
		set.observations = data;
		observations.add(set);

	}

	public List<LidarObservation> getTranslatedObservations()
	{

		List<LidarObservation> result = new LinkedList<>();
		for (ObservationSet set : observations)
		{
			double heading = Math.toRadians(set.heading - poseSource.getHeading());
			Rotation frameRotation = new Rotation(RotationOrder.XYZ, 0, 0, heading);

			DistanceXY frameTranslation = poseSource.getXyPosition().subtract(set.position);
			logger.info("Frame translation {} {}", poseSource.getXyPosition(), frameTranslation);

			for (ScanObservation observation : set.observations)
			{
				Vector3D worldFrame = observation.getVector();

				result.add(new LidarObservation(
						frameRotation.applyTo(worldFrame).subtract(frameTranslation.getVector(DistanceUnit.CM))));
			}

		}

		return result;
	}

}
