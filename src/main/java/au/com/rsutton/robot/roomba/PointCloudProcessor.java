package au.com.rsutton.robot.roomba;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.openni.PixelFormat;
import org.openni.Point3D;
import org.openni.VideoMode;

import au.com.rsutton.hazelcast.ImageMessage;
import au.com.rsutton.hazelcast.PointCloudMessage;

public class PointCloudProcessor implements PointCloudListener
{
	PointCloudProvider provider;

	Rotation cameraAngle = new Rotation(RotationOrder.XYZ, Math.toRadians(-15), 0, 0);

	PointCloudProcessor()
	{
		provider = new PointCloudProvider();
		provider.startStream(null, this);
	}

	@Override
	public void evaluatePointCloud(List<Vector3D> pointCloud, long created)
	{
		List<Vector3D> source = new LinkedList<>();

		for (Vector3D point : pointCloud)
		{
			// swap Z and Y to conform to a more robotic convention
			Vector3D rotatedVector = cameraAngle.applyTo(new Vector3D(point.getX(), point.getY(), point.getZ()));

			// Vector3D rotatedVector = cameraAngle.applyInverseTo(vector);
			source.add(rotatedVector);
		}
		PointCloudMessage pcMessage = new PointCloudMessage();
		pcMessage.setCreated(created);
		pcMessage.setPoints(source);
		pcMessage.setTopic();
		pcMessage.publish();

	}

	void evaluateColumn(List<Point3D<Float>> pointCloud)
	{

	}

	@Override
	public VideoMode chooseDepthMode(List<VideoMode> supportedModes)
	{
		VideoMode mode = supportedModes.get(0);

		for (VideoMode vm : supportedModes)
		{
			if (vm.getResolutionX() >= 600 && vm.getResolutionX() < 800)
			{
				if (vm.getPixelFormat() == PixelFormat.DEPTH_1_MM && vm.getFps() < 11)
				{
					mode = vm;
				}
			}
		}

		return mode;
	}

	public void stop()
	{
		provider.stopStream();

	}

	@Override
	public void evaluateColorFrame(BufferedImage image)
	{
		ImageMessage pcMessage = new ImageMessage();
		try
		{
			pcMessage.setImage(image);

			pcMessage.setTopic();
			pcMessage.publish();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public VideoMode chooseColorMode(List<VideoMode> supportedColorModes)
	{
		VideoMode mode = supportedColorModes.get(0);

		for (VideoMode vm : supportedColorModes)
		{
			if (vm.getResolutionX() >= 600 && vm.getResolutionX() < 800)
			{
				if (vm.getPixelFormat() == PixelFormat.RGB888 && vm.getFps() < 11)
				{
					mode = vm;
				}
			}
		}

		return mode;
	}
}
