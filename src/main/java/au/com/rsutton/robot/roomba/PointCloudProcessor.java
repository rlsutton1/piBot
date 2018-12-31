package au.com.rsutton.robot.roomba;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.openni.Point3D;
import org.openni.VideoMode;

import au.com.rsutton.hazelcast.PointCloudMessage;

public class PointCloudProcessor implements PointCloudListener
{
	PointCloudProvider provider;

	Rotation cameraAngle = new Rotation(RotationOrder.XYZ, Math.toRadians(25), 0, 0);

	PointCloudProcessor()
	{
		provider = new PointCloudProvider();
		provider.startStream(null, this);
	}

	@Override
	public void evaluatePointCloud(List<Point3D<Float>> pointCloud)
	{
		List<Vector3D> source = new LinkedList<>();

		for (Point3D<Float> point : pointCloud)
		{
			// swap Z and Y to conform to a more robotic convention
			Vector3D vector = cameraAngle
					.applyTo(new Vector3D(point.getX() / 10.0, point.getZ() / 10.0, point.getY() / 10.0));

			Vector3D rotatedVector = cameraAngle.applyInverseTo(vector);
			source.add(rotatedVector);
		}
		PointCloudMessage pcMessage = new PointCloudMessage();
		pcMessage.setPoints(source);
		pcMessage.setTopic();
		pcMessage.publish();

	}

	@Override
	public VideoMode chooseVideoMode(List<VideoMode> supportedModes)
	{
		return supportedModes.get(0);
	}

	public void stop()
	{
		provider.stopStream();

	}
}
