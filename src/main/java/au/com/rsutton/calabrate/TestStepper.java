package au.com.rsutton.calabrate;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import com.google.common.base.Stopwatch;
import com.pi4j.gpio.extension.grovePi.GrovePiProvider;
import com.pi4j.gpio.extension.lidar.LidarScanner;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import au.com.rsutton.config.Config;
import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.i2c.I2cSettings;
import au.com.rsutton.mapping.v3.linearEquasion.LinearEquasion;
import au.com.rsutton.mapping.v3.linearEquasion.LinearEquasionFactory;
import au.com.rsutton.robot.rover.Angle;
import au.com.rsutton.robot.rover.AngleUnits;
import au.com.rsutton.robot.rover.LidarObservation;
import au.com.rsutton.robot.spinner.Spinner;

public class TestStepper
{

	public TestStepper() throws IOException, InterruptedException, BrokenBarrierException, UnsupportedBusNumberException
	{
		Config config = new Config();

		GrovePiProvider grove = new GrovePiProvider(I2cSettings.busNumber, 4);

		// AdafruitPCA9685 pwm = new AdafruitPCA9685(); // 0x40 is the default
		//
		// pwm.setPWMFreq(1000);
		int offset = 200 * 8;

		Spinner driver = new Spinner((long) (offset * 1.00), grove, config);

		// danger between 6ms and 25 ms stall

		Thread.sleep(60000);

		// new LidarScanningService(grove, driver, config);
		//
		// new LidarObservation().addMessageListener(new
		// MessageListener<LidarObservation>()
		// {
		//
		// @Override
		// public void onMessage(Message<LidarObservation> message)
		// {
		// LidarObservation observation = message.getMessageObject();
		// publishScan(observation);
		// }
		// });
		//
		// scanForAndfindLines();

		// int lock = findLock(-38, 38, lidar);
		// while (true)
		// {
		// try
		// {
		// lock = findLock(lock - 6, lock + 6, lidar);
		// } catch (Exception e)
		// {
		// lock = findLock(-38, 38, lidar);
		// }
		// }

	}

	private List<Integer> findEdges(List<Vector3D> points, int edgeStepSize)
			throws InterruptedException, BrokenBarrierException, IOException
	{
		List<Integer> edges = new LinkedList<>();
		List<Integer> values = new LinkedList<>();
		int i = 0;
		for (Vector3D point : points)
		{
			int value = (int) Vector3D.distance(new Vector3D(0, 0, 0), point);
			values.add(value);
			if (values.size() > 6)
			{
				int av1 = (values.get(0) + values.get(1) + values.get(2)) / 3;
				int av2 = (values.get(3) + values.get(4) + values.get(5)) / 3;
				if (Math.abs(av1 - values.get(0)) < av1 * 0.1 && av2 > av1 + edgeStepSize)
				{
					System.out.println("Edge at " + i + " dist " + values.get(2));
					edges.add(i - 3);
				}

				values.remove(0);
			}
			i++;
		}
		return edges;

	}

	private int findLock(int min, int max, LidarScanner lidar)
			throws InterruptedException, BrokenBarrierException, IOException
	{
		List<Integer> values = new LinkedList<>();
		for (int i = min; i < max; i++)
		{
			int value = (int) Vector3D.distance(new Vector3D(0, 0, 0), lidar.scan(i));
			values.add(value);
			if (values.size() > 6)
			{
				int av1 = (values.get(0) + values.get(1) + values.get(2)) / 3;
				int av2 = (values.get(3) + values.get(4) + values.get(5)) / 3;
				if (Math.abs(av1 - values.get(0)) < av1 * 0.1 && av2 > av1 + 30)
				{
					System.out.println("Lock at " + i + " dist " + values.get(2));
					return i - 3;
				}

				values.remove(0);
			}

		}
		throw new RuntimeException("No lock");

	}

	enum LinePointAnalysis
	{
		TO_SHORT, IS_A_LINE, TOO_FEW_POINTS, NOT_A_LINE
	}

	static final int MIN_POINTS = 4;
	static final int ACCURACY_MULTIPIER = 5;

	private void scanForAndfindLines() throws InterruptedException, BrokenBarrierException, IOException
	{
		List<Vector3D> points = new LinkedList<>();
		// for (int i = min; i < max; i++)
		// {
		// points.add(lidar.scan(i));
		//
		// }

		// find crisp edges in the point scan
		List<Integer> edges = findEdges(points, 30);

		// break the list of points up into segments divided by the crisp edges
		// to aid in accurately finding the ends of the lines
		List<List<Vector3D>> segments = new LinkedList<>();

		int start = 0;
		for (int i : edges)
		{
			segments.add(points.subList(start, i));
			start = i;
		}
		if (start < points.size() - 1)
		{
			segments.add(points.subList(start, points.size() - 1));
		}

		// iterate segements looking for lines
		List<List<Vector3D>> lines = new LinkedList<>();
		for (List<Vector3D> segment : segments)
		{
			lines.addAll(findLines(segment, 7));
		}

		// show lines
		for (List<Vector3D> line : lines)
		{
			Stopwatch timer = Stopwatch.createStarted();

			SimpleRegression regression = new SimpleRegression();
			for (Vector3D point : line)
			{
				regression.addData(point.getX(), point.getY());

			}
			System.out.println("Slope " + regression.getSlope() + " intercept " + regression.getIntercept() + " s/std "
					+ regression.getSlopeStdErr() + " conf " + regression.getSlopeConfidenceInterval() + " "
					+ regression.getInterceptStdErr());

		}
	}

	private void publishScan(LidarObservation observation)
	{

		// send location out on HazelCast
		RobotLocation currentLocation = new RobotLocation();
		currentLocation.setDeadReaconingHeading(new Angle(0, AngleUnits.DEGREES));
		currentLocation.setX(new Distance(0, DistanceUnit.CM));
		currentLocation.setY(new Distance(0, DistanceUnit.CM));
		currentLocation.setSpeed(new Speed(new Distance(0, DistanceUnit.CM), Time.perSecond()));
		currentLocation.setClearSpaceAhead(new Distance(0, DistanceUnit.CM));

		List<LidarObservation> observations = new LinkedList<>();

		observations.add(observation);

		currentLocation.addObservations(observations);

		currentLocation.publish();

	}

	/**
	 * finds lines within the list of points
	 * 
	 * @param points
	 * @param accuracy
	 * @return
	 */
	List<List<Vector3D>> findLines(List<Vector3D> points, double accuracy)
	{

		List<List<Vector3D>> lines = new LinkedList<>();
		int start = 0;
		int end = start + MIN_POINTS;

		LinePointAnalysis previousState = LinePointAnalysis.NOT_A_LINE;
		while (start < points.size() && end < points.size())
		{
			List<Vector3D> subList = points.subList(start, end);
			LinePointAnalysis analysis = isALine(subList, accuracy);
			end++;

			// ignore TOO_FEW_POINTS and TO_SHORT, as we have increased the end
			// already
			if (analysis == LinePointAnalysis.NOT_A_LINE || analysis == LinePointAnalysis.IS_A_LINE)
			{
				if (analysis == LinePointAnalysis.NOT_A_LINE)
				{
					// not a line, doh
					if (previousState == LinePointAnalysis.IS_A_LINE)
					{
						// but it was a line without the last point, yay lets
						// record
						// the details of the line we found
						end -= 2;
						List<Vector3D> linePoints = new LinkedList<>();
						linePoints.addAll(points.subList(start, end + 1));
						lines.add(linePoints);

						// start looking for a new line at the end of this one
						start = end;

					} else
					{
						// it's not a line, it can't be a line even if we add
						// more points... so move the start point up one and
						// start over
						start++;
					}
					end = start + MIN_POINTS;
				}

				previousState = analysis;
			}

		}
		return lines;

	}

	/**
	 * determines if the list of points constitutes a line
	 * 
	 * @param points
	 * @param accuracy
	 * @return
	 */
	LinePointAnalysis isALine(List<Vector3D> points, double accuracy)
	{
		if (points.size() < MIN_POINTS)
		{
			return LinePointAnalysis.TOO_FEW_POINTS;
		}
		double length = Vector3D.distance(points.get(0), points.get(points.size() - 1));
		if (length < accuracy * ACCURACY_MULTIPIER)
		{
			return LinePointAnalysis.TO_SHORT;
		}
		LinearEquasion line = LinearEquasionFactory.getEquasion(points.get(0), points.get(points.size() - 1));
		for (Vector3D point : points)
		{
			if (!line.isPointOnLine(point, accuracy))
			{
				return LinePointAnalysis.NOT_A_LINE;
			}
		}
		return LinePointAnalysis.IS_A_LINE;
	}

}
