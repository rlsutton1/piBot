package au.com.rsutton.robot.roomba;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import au.com.rsutton.config.Config;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.hazelcast.SetMotion;
import au.com.rsutton.robot.lidar.LidarObservation;
import ev3dev.sensors.slamtec.model.Scan;
import ev3dev.sensors.slamtec.model.ScanDistance;

public class RoombaRobot implements RPLidarAdaptorListener, MessageListener<SetMotion>
{
	private static final int ROBOT_RADIUS = 15;

	private static final int MINIMUM_CLEARANCE = 5;

	Roomba630 roomba630;

	private RPLidarAdaptor lidar;

	private volatile boolean obsticleNear = false;

	Vector3D lidarTranslation = new Vector3D(0, -10, 0);

	Rotation lidarRotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(0));

	public void configure(Config config) throws Exception
	{

		roomba630 = new Roomba630();
		lidar = new RPLidarAdaptor(this);

		roomba630.configure(config);
		lidar.configure(config);

		SetMotion message = new SetMotion();
		message.addMessageListener(this);
	}

	/**
	 * receive HazelCast SetMotion Message
	 */
	@Override
	public void onMessage(Message<SetMotion> message)
	{
		SetMotion command = message.getMessageObject();

		if (obsticleNear)
		{
			// turn on the spot!
			Double changeHeading = command.getChangeHeading();
			if (changeHeading > 179)
			{
				changeHeading -= 360;
			}
			if (changeHeading < -179)
			{
				changeHeading += 360;
			}
			command.setChangeHeading(Math.signum(changeHeading) * 120);
		}

		roomba630.setMotion(command);

	}

	@Override
	public void receiveLidarScan(Scan scan)
	{
		List<LidarObservation> observations = new LinkedList<>();

		boolean isStartOfScan = true;

		if (scan != null)
		{
			obsticleNear = false;

			for (ScanDistance measurement : scan.getDistances())
			{
				double angle = measurement.getAngle();
				double distance = measurement.getDistance();

				Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(angle));
				Vector3D vector = new Vector3D(0, distance, 0);

				Vector3D result = rotation.applyTo(vector);

				// translate and rotation for the lidar's position on the robot
				result = lidarTranslation.add(lidarRotation.applyTo(result));

				observations.add(new LidarObservation(result));
				if (isStartOfScan)
				{
					isStartOfScan = false;
				}

				obsticleNear |= isObsticleNear(result);

			}
			if (obsticleNear)
			{
				roomba630.alarm();
			}
		}
		// need to apply translations to the scan data

		// need to change this to a different message type
		RobotLocation location = new RobotLocation();

		location.setDistanceTravelled(roomba630.getDistanceTraveled());
		location.setDeadReaconingHeading(roomba630.getAngleTurned());

		location.addObservations(observations);
		location.setBumpLeft(roomba630.getBumpLeft());
		location.setBumpRight(roomba630.getBumpRight());
		location.publish();

	}

	boolean isObsticleNear(Vector3D result)
	{
		double safeWidth = ROBOT_RADIUS + MINIMUM_CLEARANCE;
		double yExclusionZone = 35;
		boolean ret = false;
		if (result.getY() > 0 && result.getY() < yExclusionZone + ROBOT_RADIUS)
		{
			if (Math.abs(result.getX()) < safeWidth)
			{
				double distance = result.distance(Vector3D.ZERO);
				if (distance > ROBOT_RADIUS)
				{
					double scaling = 1.0 - (Math.abs(result.getX()) / safeWidth);

					if ((distance - ROBOT_RADIUS) < (yExclusionZone * scaling) + MINIMUM_CLEARANCE)
					{
						ret = true;
					}
				}
			}
		}
		return ret;
	}

	public void shutdown()
	{
		roomba630.shutdown();

	}

}
