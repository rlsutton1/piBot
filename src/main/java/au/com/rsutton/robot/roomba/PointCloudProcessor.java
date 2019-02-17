package au.com.rsutton.robot.roomba;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.openni.PixelFormat;
import org.openni.Point3D;
import org.openni.VideoMode;

import au.com.rsutton.depthcamera.PeakFinder;
import au.com.rsutton.hazelcast.ImageMessage;
import au.com.rsutton.hazelcast.PointCloudMessage;
import au.com.rsutton.mapping.probability.ProbabilityMap;

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
	public void evaluatePointCloud(List<Point3D<Float>> pointCloud)
	{
		List<Vector3D> source = new LinkedList<>();

		for (Point3D<Float> point : pointCloud)
		{
			// swap Z and Y to conform to a more robotic convention
			Vector3D rotatedVector = cameraAngle
					.applyTo(new Vector3D(point.getX() / 10.0, point.getZ() / 10.0, point.getY() / 10.0));

			// Vector3D rotatedVector = cameraAngle.applyInverseTo(vector);
			source.add(rotatedVector);
		}
		PointCloudMessage pcMessage = new PointCloudMessage();
		pcMessage.setPoints(source);
		pcMessage.setTopic();
		pcMessage.publish();

	}

	/**
	 * simple test code, remove all points where z < 0, should look a lot like a
	 * 2D lidar scan at the height of the camera
	 * 
	 * @param points
	 * @return
	 */
	public static List<Vector3D> removeGroundPlane(List<Vector3D> points)
	{
		Iterator<Vector3D> itr = points.iterator();
		while (itr.hasNext())
		{
			Vector3D next = itr.next();
			if (next.getZ() < 30)
			{
				itr.remove();
			}
		}
		return points;
	}

	public static List<Vector3D> removeGroundPlanePrototype(List<Vector3D> points)
	{

		PeakFinder peakFinder = new PeakFinder();

		double expectedDeviation = 8;
		double voidValue = 0;

		ProbabilityMap world = new ProbabilityMap(5);

		for (Vector3D vector : points)
		{
			double currentValue = world.get(vector.getX(), vector.getY());
			world.setValue(vector.getX(), vector.getY(), Math.max(currentValue, vector.getZ()));
		}

		int width = world.getMaxX() - world.getMinX();
		int height = world.getMaxY() - world.getMinY();
		double[][] image = new double[width][height];
		for (int x = world.getMinX(); x < world.getMaxX(); x += world.getBlockSize())
			for (int y = world.getMinY(); y < world.getMaxY(); y += world.getBlockSize())
				image[x - world.getMinX()][y - world.getMinY()] = world.get(x, y);

		double[][] result = peakFinder.findPeaks(image, expectedDeviation, voidValue);
		// double[][] result = image;

		List<Vector3D> pset = new LinkedList<>();
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				if (result[x][y] > 0)
					pset.add(new Vector3D((x * 2) + world.getMinX(), (y * 2) + world.getMinY(), 1));

		return pset;
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
