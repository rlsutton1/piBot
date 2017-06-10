package au.com.rsutton.hazelcast;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Test;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.pi4j.gpio.extension.pixy.Coordinate;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;

public class CalabratePixy implements MessageListener<RobotLocation>
{
	volatile private int heading = 10000;
	volatile private Distance distance;

	LinkedBlockingQueue<Collection<Coordinate>> queuedLaserData = new LinkedBlockingQueue<>();

	@Test
	public void gotoTarget() throws InstantiationException, IllegalAccessException, InterruptedException
	{

		// drive robot to gather calabration data....

		SetMotion message = new SetMotion();

		RobotLocation locationMessage = new RobotLocation();
		locationMessage.addMessageListener(this);
		System.out.println("Waiting for initial heading");
		while (heading == 10000)
		{
			Thread.sleep(100);
		}
		int setHeading = heading;
		message.setChangeHeading(0.0);
		for (int setDistance = 35; setDistance < 200; setDistance += 3)
		{
			while (!checkDistance(setDistance))
			{
				System.out.println("moving to " + setDistance);
				while (!checkDistance(setDistance))
				{
					int speed = ((int) distance.convert(DistanceUnit.CM)) - setDistance;

					if (speed > 0)
					{
						speed = Math.min(5, speed);
					} else
					{
						speed = Math.max(-5, speed);
					}
					message.setSpeed(new Speed(new Distance(speed, DistanceUnit.CM), Time.perSecond()));
					message.publish();

					Thread.sleep(100);
				}
				message.setSpeed(new Speed(new Distance(0, DistanceUnit.CM), Time.perSecond()));
				message.publish();

				System.out.println("waiting for heading to settle");
				for (int i = 0; i < 4; i++)
				{
					while (heading < setHeading + 1 && heading > setHeading - 1)
					{
						Thread.sleep(100);
					}
					Thread.sleep(500);
				}
			}
			queuedLaserData.clear();
			System.out.println("Collecting data");

			int ctr = 0;
			int points = 4;
			int samples = 15;
			while (queuedLaserData.size() < points * samples && ctr < (points * samples * 2))
			{
				Thread.sleep(250);
				ctr++;
			}
			System.out.println("Analysing data (NOT) got " + queuedLaserData.size() + " points");
			// TODO: analyse laser data

			// add entry to data for the set distance
			data.put(setDistance, new LinkedHashMap<AverageValue, AverageValue>());
			Map<AverageValue, AverageValue> distanceSet = data.get(setDistance);

			// get value sets from queue
			List<Collection<Coordinate>> tmp = new LinkedList<>();
			tmp.addAll(queuedLaserData);

			// iterate value sets
			for (Collection<Coordinate> set : tmp)
			{
				for (Coordinate values : set)
				{
					// accumulate average values;
					AverageValue txa = new AverageValue();
					txa.add((int) values.getAverageX());

					AverageValue tset = distanceSet.get(txa);
					if (tset == null)
					{

						distanceSet.put(txa, new AverageValue());
					}
					AverageValue xa = distanceSet.get(txa);
					xa.add((int) values.getAverageY());
				}
			}

			for (Entry<Integer, Map<AverageValue, AverageValue>> vx : data.entrySet())
			{
				System.out.print("Distance: " + vx.getKey());
				// add keys to a list and sort them
				List<Integer> sortedKeys = new LinkedList<>();
				for (AverageValue key : vx.getValue().keySet())
				{
					sortedKeys.add(key.getValue());
				}
				Collections.sort(sortedKeys);

				// print the ordered values
				for (Integer key : sortedKeys)
				{
					AverageValue avKey = new AverageValue();
					avKey.add(key);
					AverageValue value = vx.getValue().get(avKey);
					System.out.print(",Xangle, " + key + ", Yangle, " + value.getValue());
				}
				System.out.println("");
			}

		}

	}

	// distance -> xangle,yangle
	Map<Integer, Map<AverageValue, AverageValue>> data = new HashMap<>();

	class AverageValue
	{
		int value = 0;

		double count = 0;

		@Override
		public int hashCode()
		{
			return 1;
		}

		@Override
		public boolean equals(Object o)
		{
			AverageValue ot = (AverageValue) o;
			return getValue() + 20 > ot.getValue() && getValue() - 20 < ot.getValue();
		}

		void add(int v)
		{
			value += v;
			count++;
		}

		@Override
		public String toString()
		{
			return "" + getValue();
		}

		int getValue()
		{
			return (int) (value / count);
		}

	}

	private boolean checkDistance(int setDistance)
	{
		int dist = (int) distance.convert(DistanceUnit.CM);
		int percentageOfSetDistance = (setDistance * 4) / 100;
		percentageOfSetDistance = Math.min(2, percentageOfSetDistance);
		return dist < setDistance + percentageOfSetDistance && dist > setDistance - percentageOfSetDistance;
	}

	@Override
	public void onMessage(Message<RobotLocation> message)
	{
		RobotLocation m = message.getMessageObject();

		heading = (int) m.getDeadReaconingHeading().getDegrees();
		distance = m.getClearSpaceAhead();
		throw new RuntimeException("This code is broken, following line was commented out to allow compile");
		// queuedLaserData.add(m.getLaserData());

	}
}