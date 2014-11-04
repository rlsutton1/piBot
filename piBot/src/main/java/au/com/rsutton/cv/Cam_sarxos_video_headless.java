package au.com.rsutton.cv;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.mapping.CoordResolver;

import com.github.sarxos.webcam.Webcam;
import com.pi4j.gpio.extension.pixy.Coordinate;

public class Cam_sarxos_video_headless implements Runnable
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

	private static final int xRes = 320;
	private static final int yRes = 240;

	int deviceId;
	final CoordResolver resolver;
	private RangeFinderConfiguration rangeFinderConfig;

	public static void main(String[] args) throws InterruptedException
	{
		final ScheduledExecutorService scheduler = Executors
				.newScheduledThreadPool(1);
		RangeFinderConfiguration config0 = new RangeFinderConfiguration.Builder()
				.setCameraResolution(xRes, yRes).setYMaxDegrees(25)
				.setYZeroDegrees(155).setXFieldOfViewRangeDegrees(50)
				.setCameraLaserSeparation(95).setOrientationToRobot(-25)
				.build();

		Cam_sarxos_video_headless cam1 = new Cam_sarxos_video_headless(0,
				config0);

		long interval = 250;

		scheduler.scheduleWithFixedDelay(cam1, 0,interval, TimeUnit.MILLISECONDS);

		try
		{
			// sleep 1 second, otherwise we dont always get the correct
			// resolution on the second camera
			Thread.sleep(2000);
		} catch (InterruptedException e)
		{
			// ignoring this...
		}

		/**
		 * calibration notes:
		 * 
		 * perform calibration in this order...
		 * 
		 * cameraLaserSeparation - how far above the camera lens the laser is
		 * mounted in millimetres
		 * 
		 * cameraResolution - the resolution that the images are being captured
		 * at
		 * 
		 * xFieldOfViewRangeDegrees - of logitec c250 the value is 50
		 * 
		 * 
		 * yMaxDegrees - set yZeroDegrees to the yResoulution, then place an
		 * object such that the laser line appears at the very top of the image,
		 * adjust yMaxDegrees until the reported distance is correct.
		 * 
		 * yZeroDegrees - set up around 2 meters from a wall, adjust
		 * yZeroDegrees until the reported distance is correct
		 * 
		 */

		RangeFinderConfiguration config1 = new RangeFinderConfiguration.Builder()
				.setCameraResolution(xRes, yRes).setYMaxDegrees(25)
				.setYZeroDegrees(155).setXFieldOfViewRangeDegrees(50)
				.setCameraLaserSeparation(95).setOrientationToRobot(+25)
				.build();

		Cam_sarxos_video_headless cam2 = new Cam_sarxos_video_headless(1,
				config1);
		scheduler.scheduleWithFixedDelay(cam2,0, interval, TimeUnit.MILLISECONDS);
		Thread.sleep(60000);
	}

	ImageProcessorV4 processor = new ImageProcessorV4();

	BufferedImage img;
	Webcam webcam;

	Cam_sarxos_video_headless(int deviceId,
			RangeFinderConfiguration rangeFinderConfig)
	{
		this.deviceId = deviceId;
		this.rangeFinderConfig = rangeFinderConfig;

		resolver = new CoordResolver(rangeFinderConfig);
		webcam = Webcam.getWebcams().get(deviceId);
		webcam.setViewSize(new Dimension(xRes, yRes));
		webcam.open();

	}

	public void run()
	{
		try
		{
			long start = System.currentTimeMillis();
			img = webcam.getImage();

			if (img != null)
			{
				System.out.println("Capture took: "
						+ (System.currentTimeMillis() - start));

				RobotLocation messageObject = new RobotLocation();
				messageObject.setHeading(0);
				messageObject.setX(new Distance(0, DistanceUnit.CM));
				messageObject.setY(new Distance(0, DistanceUnit.CM));

				Collection<Coordinate> rangeData = new LinkedList<>();
				Map<Integer, Integer> data = processor.processImage(img);
				for (Entry<Integer, Integer> value : data.entrySet())
				{
					System.out.print("Y:" + value.getValue() + " ");
					System.out.println(resolver.convertImageXYtoAbsoluteXY(
							value.getKey(), value.getValue()));
					rangeData.add(new Coordinate(value.getKey(), value
							.getValue()));
				}

				CameraRangeData cameraRangeData = new CameraRangeData(
						rangeFinderConfig, rangeData);
				messageObject.setCameraRangeData(cameraRangeData);
				messageObject.publish();

			}

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}