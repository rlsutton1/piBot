package au.com.rsutton.robot.roomba;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import au.com.rsutton.config.Config;
import au.com.rsutton.hazelcast.LidarScan;
import au.com.rsutton.hazelcast.SetMotion;
import au.com.rsutton.robot.lidar.LidarObservation;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;
import au.com.rsutton.units.Speed;
import au.com.rsutton.units.Time;
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

	volatile double nearestObsticle;

	private PointCloudProcessor pointCloudProcessor = null;

	public void configure(Config config) throws Exception
	{

		String lidarPort = config.loadSetting(RPLidarAdaptor.RPLIDAR_USB_PORT, "/dev/ttyUSB0");
		String roombaPort = config.loadSetting(Roomba630.ROOMBA_USB_PORT, "/dev/ttyUSB1");

		System.out.println("Found Roomba on port: " + roombaPort + " Lidar on port: " + lidarPort);

		lidar = new RPLidarAdaptor(this);
		roomba630 = new Roomba630();

		roomba630.configure(config, roombaPort);

		lidar.configure(config, lidarPort);

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
			command.setTurnRadius((long) Math.signum(command.getTurnRadius()));
		}

		// cap speed based on distance to nearest obstacle
		double cms = command.getSpeed().getSpeed(DistanceUnit.CM, TimeUnit.SECONDS);
		cms = Math.min(cms, Math.max(3, nearestObsticle));
		command.setSpeed(new Speed(new Distance(cms, DistanceUnit.CM), Time.perSecond()));

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
			double nearest = 10000;

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
				if (distance > ROBOT_RADIUS)
				{
					nearest = Math.min(nearest, distance - ROBOT_RADIUS);
				}

			}

			nearestObsticle = nearest;

			if (obsticleNear)
			{
				roomba630.alarm();
			}
			LidarScan location = new LidarScan();
			location.addObservations(observations);
			location.setStartTime(scan.getStartTime());
			location.setEndTime(scan.getEndTime());
			location.publish();
		}

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
		if (pointCloudProcessor != null)
		{
			pointCloudProcessor.stop();
		}
	}

	public void startDepthCamera()
	{
		pointCloudProcessor = new PointCloudProcessor();

	}

}
