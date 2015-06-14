package au.com.rsutton.cv;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.hazelcast.ImageMessage;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.mapping.CoordResolver;
import au.com.rsutton.mapping.XY;
import au.com.rsutton.robot.rover.Angle;
import au.com.rsutton.robot.rover.AngleUnits;

import com.pi4j.gpio.extension.pixy.Coordinate;

public class LaserRangeFinder implements ImageStreamProcessor
{

	private static final int xRes = 544;
	private static final int yRes = 288;

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
				messageObject.setHeading(new Angle(0, AngleUnits.DEGREES));
				messageObject.setX(new Distance(0, DistanceUnit.CM));
				messageObject.setY(new Distance(0, DistanceUnit.CM));

				messageObject.setCameraRangeData(cameraRangeData);
				messageObject.publish();
			}
		};

		start(reporter);
	}

	static public void start(RobotLocationReporter reporter)
			throws InterruptedException
	{
		System.out.println("Starting up");

		// right camera
		final RangeFinderConfiguration config0 = new RangeFinderConfiguration.Builder()
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

		ImageStreamer imageStreamer0 = new ImageStreamer(0);
		LaserRangeFinder cam0 = new LaserRangeFinder(imageStreamer0, config0,
				reporter);
		imageStreamer0.addListener(new ImageStreamProcessor()
		{
			long lastImageSent = 0;

			@Override
			public void processImage(BufferedImage img)
			{
				if (lastImageSent + 5000 < System.currentTimeMillis())
				{
					lastImageSent = System.currentTimeMillis();
					ImageMessage message = new ImageMessage(img, config0);
					message.publish();
				}
			}
		});

		// sleep 1 second, otherwise we dont always get the correct
		// resolution on the second camera
		Thread.sleep(2000);

		LaserRangeFinder cam1 = new LaserRangeFinder(new ImageStreamer(1),
				config1, reporter);
		// Thread.sleep(60000);
	}

	final ImageProcessorV4 processor;

	private RobotLocationReporter reporter;

	LaserRangeFinder(ImageStreamer imageStreamer,
			RangeFinderConfiguration rangeFinderConfig,
			RobotLocationReporter reporter)
	{
		this.rangeFinderConfig = rangeFinderConfig;
		this.reporter = reporter;
		processor = new ImageProcessorV4(new CoordResolver(rangeFinderConfig));

		imageStreamer.addListener(this);

	}

	@Override
	public void processImage(BufferedImage img)
	{
		try
		{
			long start = System.currentTimeMillis();

			CoordResolver resolver = new CoordResolver(rangeFinderConfig);
			// System.out.println("Capture took: "
			// + (System.currentTimeMillis() - start));

			Collection<Coordinate> rangeData = new LinkedList<>();
			Map<Integer, Integer> data = Collections.EMPTY_MAP;
			data = processor.processImage(img);

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
						// System.out.print(" " + convertedXY);
						rangeData.add(new Coordinate(value.getKey(), value
								.getValue()));
					}
				}
			}
			// System.out.println();

			CameraRangeData cameraRangeData = new CameraRangeData(
					rangeFinderConfig, rangeData);
			reporter.report(cameraRangeData);

		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

}