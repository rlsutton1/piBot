package au.com.rsutton.cv;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.mapping.CoordResolver;

import com.pi4j.gpio.extension.pixy.Coordinate;

public class Cam_video implements Runnable
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
	private static final int yRes = 176;

	int deviceId;
	final CoordResolver resolver;
	private RangeFinderConfiguration rangeFinderConfig;

	public static void main(String[] args)
	{
		RangeFinderConfiguration config0 = new RangeFinderConfiguration.Builder()
				.setCameraResolution(xRes, yRes).setYMaxDegrees(25)
				.setYZeroDegrees(155).setXFieldOfViewRangeDegrees(50)
				.setCameraLaserSeparation(95).setOrientationToRobot(-25)
				.build();

		new Thread(new Cam_video(0, config0)).start();
		try
		{
			// sleep 1 second, otherwise we dont always get the correct
			// resolution on the second camera
			Thread.sleep(1000);
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
				.setCameraLaserSeparation(95).setOrientationToRobot(+25).build();

		new Thread(new Cam_video(1, config1)).start();
	}

	Cam_video(int deviceId, RangeFinderConfiguration rangeFinderConfig)
	{
		this.deviceId = deviceId;
		this.rangeFinderConfig = rangeFinderConfig;

		resolver = new CoordResolver(rangeFinderConfig);

	}

	public void run()
	{
		try
		{
			// Create canvas frame for displaying video.
			CanvasFrame canvas = new CanvasFrame("VideoCanvas");

			// Set Canvas frame to close on exit
			canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

			FrameGrabber grabber = new OpenCVFrameGrabber(deviceId);

			grabber.setImageWidth(xRes);
			grabber.setImageHeight(yRes);

			grabber.setTriggerMode(true);
			grabber.start();

			ImageProcessorV4 processor = new ImageProcessorV4();

			boolean firstFrame = true;

			// grabber.setFrameRate(30);

			grabber.setNumBuffers(1);

			IplImage img;
			while (true)
			{
				Thread.sleep(100);

				grabber.trigger();
				// grab video frame to IplImage img
				img = grabber.grab();
				if (firstFrame)
				{
					// Set canvas size as per dimentions of video frame.
					canvas.setCanvasSize(img.width(), img.height());
					firstFrame = false;
				}
				if (img != null)
				{

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

					// Show video frame in canvas
					canvas.showImage(img);
				}

			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}