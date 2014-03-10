package au.com.rsutton.robot;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;

public class DeadReconingWithQuadrature implements HeadingProvider
{

	private final static DistanceUnit unit = DistanceUnit.MM;

	private static final double WHEEL_RADIUS = 21; // MM

	private static final double DISTANCE_BETWEEN_WHEELS = 220; // MM

	private static final double TICKS_PER_ROTATION = 2.0d * WHEEL_RADIUS
			* Math.PI; // MM

	List<QuadraturePointData> historicalPoints = new LinkedList<QuadraturePointData>();

	double initialX = 0;
	double initialY = 0;
	double heading;
	double initialLeftWheelReading = 0;
	double initialRightWheelReading = 0;

	double currentLeftWheelReading = 0;
	double currentRightWheelReading = 0;

	private Set<HeadingListener> listeners = new HashSet<HeadingListener>();


	long lastEvent = 0;

	public synchronized void resetLocation(Distance x, Distance y, int heading)
	{
		initialX = x.convert(unit);
		initialY = y.convert(unit);
		initialLeftWheelReading = currentLeftWheelReading;
		initialRightWheelReading = currentRightWheelReading;
		this.heading = heading;
		for (HeadingListener listener : listeners)
		{
			listener.headingChanged(heading);
		}
	}

	public synchronized void updateLocation(Distance distance,
			Distance distance2)
	{

		try
		{

			double plw = currentLeftWheelReading;
			if (distance != null)
			{
				currentLeftWheelReading = distance.convert(unit);
			}
			double prw = currentRightWheelReading;
			if (distance2 != null)
			{
				currentRightWheelReading = distance2.convert(unit);
			}

			if (lastEvent < System.currentTimeMillis() - 100)
			{
				// it's more than 100ms since we last did the trig, so do it
				// now.
				if (Math.abs(prw - currentRightWheelReading) > 0.1
						|| Math.abs(plw - currentLeftWheelReading) > 0.1)
				{
					lastEvent = System.currentTimeMillis();
					historicalPoints.add(new QuadraturePointData(
							currentLeftWheelReading, currentRightWheelReading,
							heading));
					QuadraturePointData oldestPoint = null;
					if (historicalPoints.size() > 80)
					{
						oldestPoint = historicalPoints.remove(0);
					} else
					{
						oldestPoint = historicalPoints.get(0);
					}
					double it1 = oldestPoint.getLeft()
							- currentLeftWheelReading;
					double it2 = oldestPoint.getRight()
							- currentRightWheelReading;

					double deltaHeading = Math.toDegrees(2.0d * Math.PI
							* (WHEEL_RADIUS / DISTANCE_BETWEEN_WHEELS)
							* ((it1 - it2) / TICKS_PER_ROTATION));

					double t1 = initialLeftWheelReading
							- currentLeftWheelReading;
					double t2 = initialRightWheelReading
							- currentRightWheelReading;

					heading = oldestPoint.getHeading() + deltaHeading;
					initialX += Math.sin(Math.toRadians(heading))
							* ((t1 + t2) / 2.0d);
					initialY += -Math.cos(Math.toRadians(heading))
							* ((t1 + t2) / 2.0d);

					initialLeftWheelReading = currentLeftWheelReading;
					initialRightWheelReading = currentRightWheelReading;

					for (HeadingListener listener : listeners)
					{
						listener.headingChanged((int) heading);
					}
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public synchronized Distance getX()
	{

		return new Distance(initialX, unit);
	}

	public synchronized Distance getY()
	{

		return new Distance(initialY, unit);

	}

	public synchronized int getHeading()
	{

		return (int) heading;
	}

	@Override
	public void setCorrectedHeading(int heading)
	{
		this.heading = heading;

	}

	@Override
	public void addHeadingListener(HeadingListener robot)
	{
		listeners.add(robot);

	}

	@Override
	public boolean isCalabrated()
	{

		return true;
	}

}
