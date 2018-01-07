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
	Roomba630 roomba630;

	private RPLidarAdaptor lidar;

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

		roomba630.setMotion(command);

	}

	@Override
	public void receiveLidarScan(Scan scan)
	{
		List<LidarObservation> observations = new LinkedList<>();

		boolean isStartOfScan = true;

		if (scan != null)
		{

			for (ScanDistance measurement : scan.getDistances())
			{
				double angle = measurement.getAngle();
				double distance = measurement.getDistance();

				Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(angle));
				Vector3D vector = new Vector3D(0, distance, 0);

				Vector3D result = rotation.applyTo(vector);

				// translate and rotation for the lidar's position on the robot
				result = lidarTranslation.add(lidarRotation.applyTo(result));

				observations.add(new LidarObservation(result, isStartOfScan));
				if (isStartOfScan)
				{
					isStartOfScan = false;
				}
			}
		}
		// need to apply translations to the scan data

		// need to change this to a different message type
		RobotLocation location = new RobotLocation();

		location.setDistanceTravelled(roomba630.getDistanceTraveled());
		location.setDeadReaconingHeading(roomba630.getAngleTurned());

		location.addObservations(observations);
		location.publish();

	}

	public void shutdown()
	{
		roomba630.shutdown();

	}

}
