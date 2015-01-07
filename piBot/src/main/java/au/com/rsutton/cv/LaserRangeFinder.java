package au.com.rsutton.cv;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.mapping.CoordResolver;
import au.com.rsutton.mapping.XY;
import au.edu.jcu.v4l4j.CaptureCallback;
import au.edu.jcu.v4l4j.DeviceInfo;
import au.edu.jcu.v4l4j.FrameInterval;
import au.edu.jcu.v4l4j.FrameInterval.DiscreteInterval;
import au.edu.jcu.v4l4j.ImageFormat;
import au.edu.jcu.v4l4j.JPEGFrameGrabber;
import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.VideoDevice;
import au.edu.jcu.v4l4j.VideoFrame;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;

import com.pi4j.gpio.extension.pixy.Coordinate;

public class LaserRangeFinder implements Runnable, CaptureCallback
{

	// v4l2-ctl -d /dev/video0 --list-framesizes=YUYV
	// ioctl: VIDIOC_ENUM_FRAMESIZES
	// Size: Discrete 640x480
	// Size: Discrete 160x120
	// Size: Discrete 176x144
	// Size: Discrete 320x176
	// Size: Discrete 320x240
	// Size: Discrete 352x288
	// Size: Discrete 432x240
	// Size: Discrete 544x288
	// Size: Discrete 640x360
	// Size: Discrete 752x416
	// Size: Discrete 800x448
	// Size: Discrete 800x600
	// Size: Discrete 864x480
	// Size: Discrete 960x544
	// Size: Discrete 960x720
	// Size: Discrete 1024x576
	// Size: Discrete 1184x656
	// Size: Discrete 1280x720
	// Size: Discrete 1280x960

	private static final int xRes = 544;
	private static final int yRes = 288;

	// private static final int xRes = 320;
	// private static final int yRes = 240;

	int deviceId;
	// CoordResolver resolver;

	/**
	 * calibration notes:
	 * 
	 * perform calibration in this order...
	 * 
	 * cameraLaserSeparation - how far above the camera lens the laser is
	 * mounted in millimetres
	 * 
	 * cameraResolution - the resolution that the images are being captured at
	 * 
	 * xFieldOfViewRangeDegrees - of logitec c250 the value is 50
	 * 
	 * 
	 * yMaxDegrees - set yZeroDegrees to the yResoulution, then place an object
	 * such that the laser line appears at the very top of the image, adjust
	 * yMaxDegrees until the reported distance is correct.
	 * 
	 * yZeroDegrees - set up around 2 meters from a wall, adjust yZeroDegrees
	 * until the reported distance is correct
	 * 
	 */
	private RangeFinderConfiguration rangeFinderConfig;

	public static void main(String[] args) throws InterruptedException
	{
		RobotLocationReporter reporter = new RobotLocationReporter()
		{
			public void report(CameraRangeData cameraRangeData)
			{

				RobotLocation messageObject = new RobotLocation();
				messageObject.setHeading(0);
				messageObject.setX(new Distance(0, DistanceUnit.CM));
				messageObject.setY(new Distance(0, DistanceUnit.CM));

				messageObject.setCameraRangeData(cameraRangeData);
				messageObject.publish();
			}
		};

		start(reporter);
	}

	static public void start(RobotLocationReporter reporter)
	{
		System.out.println("Starting up");

		// right camera
		RangeFinderConfiguration config0 = new RangeFinderConfiguration.Builder()
				.setCameraResolution(xRes, yRes).setYMaxDegrees(24)
				.setYZeroDegrees(242).setXFieldOfViewRangeDegrees(60)
				.setCameraLaserSeparation(110).setOrientationToRobot(-28)
				.build();

		// left camera
		RangeFinderConfiguration config1 = new RangeFinderConfiguration.Builder()
				.setCameraResolution(xRes, yRes).setYMaxDegrees(24)
				.setYZeroDegrees(244).setXFieldOfViewRangeDegrees(60)
				.setCameraLaserSeparation(95).setOrientationToRobot(+28)
				.build();

		LaserRangeFinder cam1 = new LaserRangeFinder(0, config0, reporter);

		new Thread(cam1, "Cam 1").start();

		try
		{
			// sleep 1 second, otherwise we dont always get the correct
			// resolution on the second camera
			Thread.sleep(2000);
		} catch (InterruptedException e)
		{
			// ignoring this...
		}

		LaserRangeFinder cam2 = new LaserRangeFinder(1, config1, reporter);
		new Thread(cam2, "Cam 2").start();
		// Thread.sleep(60000);
	}

	ImageProcessorV4 processor = new ImageProcessorV4();

	BufferedImage img;
	private JPEGFrameGrabber frameGrabber;
	private RobotLocationReporter reporter;

	LaserRangeFinder(int deviceId, RangeFinderConfiguration rangeFinderConfig,
			RobotLocationReporter reporter)
	{
		this.deviceId = deviceId;
		this.rangeFinderConfig = rangeFinderConfig;
		this.reporter = reporter;

	}

	@Override
	public void exceptionReceived(V4L4JException arg0)
	{
		arg0.printStackTrace();

	}

	@Override
	public void nextFrame(VideoFrame frame)
	{

		try
		{
			long start = System.currentTimeMillis();
			img = frame.getBufferedImage();

			if (img != null)
			{
				CoordResolver resolver = new CoordResolver(rangeFinderConfig);
				// System.out.println("Capture took: "
				// + (System.currentTimeMillis() - start));

				Collection<Coordinate> rangeData = new LinkedList<>();
				Map<Integer, Integer> data = processor.processImage(img);

				for (Entry<Integer, Integer> value : data.entrySet())
				{
					if (value.getValue() != 0 && value.getKey() != 0)
					{
						XY convertedXY = resolver.convertImageXYtoAbsoluteXY(
								value.getKey(), value.getValue());
						if (convertedXY.getX() != 0 && convertedXY.getY() != 0
								&& Math.abs(convertedXY.getX()) < 4000
								&& Math.abs(convertedXY.getY()) < 4000)
						{
							// System.out.print(" Y:" + value.getValue() + " ");
							System.out.print(" " + convertedXY);
							rangeData.add(new Coordinate(value.getKey(), value
									.getValue()));
						}
					}
				}
				System.out.println();

				CameraRangeData cameraRangeData = new CameraRangeData(
						rangeFinderConfig, rangeData);
				reporter.report(cameraRangeData);
			}

		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			frame.recycle();
		}

	}

	CountDownLatch latch = new CountDownLatch(1);

	public void stop()
	{
		latch.countDown();
	}

	@Override
	public void run()
	{
		// resolver = new CoordResolver(rangeFinderConfig);
		VideoDevice webcam = null;
		try
		{
			System.out.println("Instanced");

			String dev = "/dev/video" + deviceId;
			webcam = new VideoDevice(dev);
			DeviceInfo di = webcam.getDeviceInfo();

			List<ImageFormat> formats = di.getFormatList()
					.getJPEGEncodableFormats();
			for (ImageFormat format : formats)
			{
				System.out.println(format);
			}
			FrameInterval resolution = di.listIntervals(formats.get(0), xRes,
					yRes);
			List<DiscreteInterval> intervals = resolution
					.getDiscreteIntervals();
			for (DiscreteInterval interval : intervals)
			{
				System.out.println(interval);
			}

			frameGrabber = webcam.getJPEGFrameGrabber(xRes, yRes, 0,
					V4L4JConstants.STANDARD_WEBCAM, 80);
			frameGrabber.setCaptureCallback(this);
			// width = frameGrabber.getWidth();
			// height = frameGrabber.getHeight();
			frameGrabber.setFrameInterval(1, 5); // tenth of a second
			frameGrabber.startCapture();
			latch.await();

		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			if (frameGrabber != null)
			{
				frameGrabber.stopCapture();
			}
			if (webcam != null)
			{
				webcam.releaseFrameGrabber();
				webcam.release();
			}
		}

	}
}