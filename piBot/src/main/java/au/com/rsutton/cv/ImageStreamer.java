package au.com.rsutton.cv;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

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

public class ImageStreamer implements Runnable, CaptureCallback
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

	public static void main(String[] args) throws InterruptedException
	{

		start(0);
	}

	static public void start(int deviceId)
	{
		System.out.println("Starting up");

		ImageStreamer cam1 = new ImageStreamer(deviceId);

		new Thread(cam1, "Cam " + deviceId).start();

	}

	BufferedImage img;
	private JPEGFrameGrabber frameGrabber;
	private List<ImageStreamProcessor> imageProcessor = new LinkedList<>();

	ImageStreamer(int deviceId)
	{
		this.deviceId = deviceId;

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
			img = frame.getBufferedImage();

			if (img != null)
			{
				for (ImageStreamProcessor processor : imageProcessor)
				{
					processor.processImage(img);
				}

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

	public void addListener(ImageStreamProcessor laserRangeFinder)
	{
		// TODO Auto-generated method stub

	}
}