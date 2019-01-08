package au.com.rsutton.robot.roomba;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openni.CoordinateConverter;
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

public class PointCloudProvider
{

	private Device device;

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
		depthStream.addNewFrameListener(colorFrameListener(listener, depthStream));

		VideoStream colorStream = VideoStream.create(device, SensorType.COLOR);
		List<VideoMode> supportedColorModes = colorStream.getSensorInfo().getSupportedVideoModes();
		colorStream.setVideoMode(listener.chooseColorMode(supportedColorModes));
		colorStream.start();
		colorStream.addNewFrameListener(pointCloudFrameListener(listener, colorStream));

	}

	private NewFrameListener colorFrameListener(PointCloudListener listener, VideoStream colorStream)
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
					res.setRGB(pos % height, pos / width, rgb);
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
				// System.out.println("ms per frame " +
				// (System.currentTimeMillis() - lastTime));
				lastTime = System.currentTimeMillis();

			}
		};
	}

	private NewFrameListener pointCloudFrameListener(final PointCloudListener listener, VideoStream mVideoStream)
	{
		return new NewFrameListener()
		{
			long lastTime = System.currentTimeMillis();

			@Override
			public void onFrameReady(VideoStream stream)
			{
				VideoFrameRef frame = stream.readFrame();

				PixelFormat pixelFormat = frame.getVideoMode().getPixelFormat();
				if (pixelFormat != PixelFormat.DEPTH_1_MM)
				{
					System.out.println("Pixel Fromat is not the expeceted DEPTH_1_MM, actual is :" + pixelFormat);
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
							Point3D<Float> point = CoordinateConverter.convertDepthToWorld(mVideoStream, x, y, z);

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
				// System.out.println("ms per frame " +
				// (System.currentTimeMillis() - lastTime));
				lastTime = System.currentTimeMillis();

			}
		};
	}

	void stopStream()
	{
		device.close();
		OpenNI.shutdown();

	}

	public static void main(String s[]) throws InterruptedException
	{

		System.out.println(System.getProperty("java.library.path"));
		PointCloudProvider test = new PointCloudProvider();
		test.startStream(s, new PointCloudListener()
		{

			@Override
			public void evaluatePointCloud(List<Point3D<Float>> pointCloud)
			{
				boolean[][] flatWorld = new boolean[80][40];
				for (Point3D<Float> point : pointCloud)
				{
					int x = (int) (point.getX() / 100) + 40;
					int y = (int) (point.getZ() / 100);
					if (x >= 0 && x < 80 && y >= 0 && y < 40)
					{
						flatWorld[x][y] = true;
					}
				}

				for (int x = 0; x < 80; x++)
				{
					for (int y = 0; y < 40; y++)
					{
						if (flatWorld[x][y])
						{
							System.out.print("X");
						} else
						{
							System.out.print(" ");
						}
					}
					System.out.println("");
				}

			}

			@Override
			public VideoMode chooseDepthMode(List<VideoMode> supportedModes)
			{
				return supportedModes.get(0);
			}

			@Override
			public void evaluateColorFrame(BufferedImage res)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public VideoMode chooseColorMode(List<VideoMode> supportedColorModes)
			{
				// TODO Auto-generated method stub
				return null;
			}
		});

		TimeUnit.SECONDS.sleep(100);

		test.stopStream();

	}

}
