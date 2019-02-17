package au.com.rsutton.robot.roomba;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openni.Device;
import org.openni.DeviceInfo;
import org.openni.OpenNI;
import org.openni.PixelFormat;
import org.openni.Point3D;
import org.openni.SensorType;
import org.openni.VideoFrameRef;
import org.openni.VideoMode;
import org.openni.VideoStream;
import org.openni.VideoStream.NewFrameListener;

import com.hazelcast.core.HazelcastInstanceNotActiveException;

import au.com.rsutton.hazelcast.DataLogValue;

public class PointCloudProvider
{

	private Device device;

	Logger logger = LogManager.getLogger();

	void startStream(String s[], final PointCloudListener listener)
	{
		// initialize OpenNI
		OpenNI.initialize();

		String uri;

		if (s != null && s.length > 0)
		{
			uri = s[0];
		} else
		{
			List<DeviceInfo> devicesInfo = OpenNI.enumerateDevices();
			if (devicesInfo.isEmpty())
			{
				System.out.println("No device is connected");
				return;
			}
			uri = devicesInfo.get(0).getUri();
		}

		device = Device.open(uri);

		VideoStream depthStream = VideoStream.create(device, SensorType.DEPTH);
		List<VideoMode> supportedDepthModes = depthStream.getSensorInfo().getSupportedVideoModes();
		depthStream.setVideoMode(listener.chooseDepthMode(supportedDepthModes));
		depthStream.start();
		depthStream.addNewFrameListener(pointCloudFrameListener(listener));

		VideoStream colorStream = VideoStream.create(device, SensorType.COLOR);
		List<VideoMode> supportedColorModes = colorStream.getSensorInfo().getSupportedVideoModes();
		colorStream.setVideoMode(listener.chooseColorMode(supportedColorModes));
		colorStream.start();
		colorStream.addNewFrameListener(colorFrameListener(listener));

	}

	private NewFrameListener colorFrameListener(PointCloudListener listener)
	{
		return new NewFrameListener()
		{
			long lastTime = System.currentTimeMillis();

			@Override
			public void onFrameReady(VideoStream stream)
			{
				VideoFrameRef frame = stream.readFrame();

				PixelFormat pixelFormat = frame.getVideoMode().getPixelFormat();

				if (pixelFormat != PixelFormat.RGB888)
				{
					System.out.println("Pixel Fromat is not the expeceted DEPTH_1_MM, actual is :" + pixelFormat);
				}

				final BufferedImage res = new BufferedImage(frame.getWidth(), frame.getHeight(),
						BufferedImage.TYPE_INT_RGB);

				int width = frame.getWidth();
				int height = frame.getHeight();
				ByteBuffer frameData = frame.getData().order(ByteOrder.LITTLE_ENDIAN);

				int pos = 0;

				while (frameData.remaining() > 0)
				{
					int red = frameData.get() & 0xFF;
					int green = frameData.get() & 0xFF;
					int blue = frameData.get() & 0xFF;
					int rgb = 0xFF000000 | (red << 16) | (green << 8) | blue;
					res.setRGB(pos % width, pos / width, rgb);
					pos++;
				}

				frame.release();
				try
				{
					listener.evaluateColorFrame(res);
				} catch (HazelcastInstanceNotActiveException e)
				{
					// we've been shut down
					stopStream();
				}
				logger.debug("ms per color frame " + (System.currentTimeMillis() - lastTime));
				lastTime = System.currentTimeMillis();

			}
		};
	}

	private NewFrameListener pointCloudFrameListener(final PointCloudListener listener)
	{
		return new NewFrameListener()
		{
			long lastTime = System.currentTimeMillis();

			@Override
			public void onFrameReady(VideoStream stream)
			{
				VideoFrameRef frame = stream.readFrame();

				new DataLogValue("FOV h", "" + stream.getHorizontalFieldOfView()).publish();
				new DataLogValue("FOV v", "" + stream.getVerticalFieldOfView()).publish();

				PixelFormat pixelFormat = frame.getVideoMode().getPixelFormat();
				PixelFormat expectedFormat = PixelFormat.DEPTH_1_MM;
				if (pixelFormat != expectedFormat)
				{
					System.out.println(
							"Pixel Fromat is not the expeceted " + expectedFormat + ", actual is :" + pixelFormat);
				}

				int width = frame.getWidth();
				int height = frame.getHeight();

				// System.out.println("Width: " + width + " Height: " + height);

				ShortBuffer sb = frame.getData().order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
				sb.rewind();
				int z = 0;

				List<Point3D<Float>> pointCloud = new LinkedList<>();

				for (int y = 0; y < height; y += 10)
				{
					for (int x = 0; x < width; x += 10)
					{
						z = sb.get(x + (y * width));
						if (z < 0)
						{
							z = 65536 + z;
						}
						if (z > 0)
						{
							// broken with RealSense D435 <br>
							// Point3D<Float> point =
							// CoordinateConverter.convertDepthToWorld(stream,
							// x, y, z);

							Point3D<Float> point = convertDepthToWorld(stream, x, y, z);

							pointCloud.add(point);
						}
					}
				}

				frame.release();
				try
				{
					listener.evaluatePointCloud(pointCloud);
				} catch (HazelcastInstanceNotActiveException e)
				{
					// we've been shut down
					stopStream();
				}
				logger.debug("ms per depth frame " + (System.currentTimeMillis() - lastTime));
				lastTime = System.currentTimeMillis();

			}
		};
	}

	Point3D<Float> convertDepthToWorld(VideoStream stream, int x, int y, int z)
	{
		float scaling = 1.35f;

		float hfov = (stream.getHorizontalFieldOfView());
		float vfov = stream.getVerticalFieldOfView();

		VideoMode videoMode = stream.getVideoMode();
		Double xResolution = new Double(videoMode.getResolutionX());
		Double yResolution = new Double(videoMode.getResolutionY());

		double halfX = xResolution / 2.0;
		double yAxisRotation = -hfov * ((x - halfX) / xResolution);
		double halfY = yResolution / 2.0;
		double xAxisRotation = vfov * ((y - halfY) / yResolution);

		Vector3D vector = new Rotation(RotationOrder.XYZ, xAxisRotation, yAxisRotation, 0)
				.applyTo(new Vector3D(0, 0, z * scaling));
		return new Point3D<>(new Float(vector.getX()), new Float(vector.getY()), new Float(vector.getZ()));
	}

	void stopStream()
	{
		device.close();
		OpenNI.shutdown();

	}

}
