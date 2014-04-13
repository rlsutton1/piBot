package au.com.rsutton.entryPoint.sonar;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.rsutton.entryPoint.controllers.ServoController;
import au.com.rsutton.entryPoint.trig.Point;

import com.pi4j.gpio.extension.adafruit.ADS1115;
import com.pi4j.gpio.extension.adafruit.Adafruit16PwmPin;
import com.pi4j.gpio.extension.adafruit.Adafruit16PwmProvider;
import com.pi4j.gpio.extension.adafruit.PwmPin;
import com.pi4j.io.gpio.PinMode;

public class FullScan
{

	private ServoController controller1;

	private Sonar sonar;

	private ScanningSonarListener listener;

	public FullScan(Adafruit16PwmProvider provider) throws IOException
	{

		ADS1115 ads = new ADS1115(1, 0x48);

//		sonar = new Sonar(0.1, 2880, 0);
//		ads.addListener(sonar);

		provider.export(Adafruit16PwmPin.GPIO_08, PinMode.PWM_OUTPUT);

		PwmPin servoPin1 = new PwmPin(provider, Adafruit16PwmPin.GPIO_08);

		controller1 = new ServoController(servoPin1, 81, 307, 1);

		System.out.println("Starting Full Scan sonar");

		Map<Point, List<Point>> matrix = new HashMap<Point, List<Point>>();

		try
		{
			SonarResolver resolver = new SonarResolver();

			controller1.setOutput(-90);
			waitForPosition();

			Point p1 = null;
			Point p2 = null;
			Point p3 = null;
			// 15 degrees = 50cm/1mtrs
//			for (int angle = -90; angle <= 90; angle += 5)
//			{
//				Distance distance = sonar.getCurrentDistance();
//				PointData pointData = resolver.createPointData(angle, distance);
//				System.out.println(distance + " " + angle + " "
//						+ pointData.getX() + " " + pointData.getY());

//				p3 = p2;
//				p2 = p1;
//				p1 = new Point(pointData.getX(), pointData.getY());
//				if (p1 != null && p2 != null && p3 != null)
//				{
//					System.out.println(TrigMath.pointsFormLine(p1, p2, p3, .1));
//							
//				}
//				
//
//				Point coarseGridPoint = new Point(new Distance(
//						(int) (pointData.getX().convert(DistanceUnit.CM)) / 25,
//						DistanceUnit.CM), new Distance(
//						(int) (pointData.getY().convert(DistanceUnit.CM)) / 25,
//						DistanceUnit.CM));
//				System.out.println(coarseGridPoint);
//				List<Point> points = matrix.get(coarseGridPoint);
//				if (points == null)
//				{
//					points = new LinkedList<Point>();
//					matrix.put(coarseGridPoint, points);
//				}
//				points.add(pointData.getPoint());
//				controller1.setOutput(angle);
//				waitForPosition();
//
//			}
//
//			// only up to 30cm
//			for (int y = 0; y < 35; y++)
//			{
//				for (int x = -25; x < 25; x++)
//				{
//					Point coarsePoint = new Point(new Distance(x,
//							DistanceUnit.CM), new Distance(y, DistanceUnit.CM));
//					if (x == 0 && y == 0)
//					{
//						System.out.print("*");
//					} else
//					{
//						List<Point> point = matrix.get(coarsePoint);
//						if (point != null)
//						{
//							System.out.print(point.size());
//						} else
//						{
//							System.out.print(" ");
//						}
//					}
//
//				}
//				System.out.println("");
//			}

		} catch (Throwable e)
		{
			e.printStackTrace();
		}

	}

	private void waitForPosition() throws InterruptedException
	{
//		Distance lastValue = sonar.getCurrentDistance();
//
//		int last = (int) (lastValue.convert(DistanceUnit.CM));
//		Thread.sleep(100);
//		int current = (int) (sonar.getCurrentDistance()
//				.convert(DistanceUnit.CM));
//		while (last > current + 2 || last < current - 2)
//		{
//			last = current;
//			Thread.sleep(100);
//			current = (int) (sonar.getCurrentDistance()
//					.convert(DistanceUnit.CM));
//		}
	}
}
