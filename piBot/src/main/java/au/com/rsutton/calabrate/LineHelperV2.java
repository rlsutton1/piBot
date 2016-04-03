package au.com.rsutton.calabrate;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;

public class LineHelperV2 extends RandomPairSelection<Vector3D>
{

	private double ratio;

	private Integer result;

	@Test
	public void test1()
	{

		String resultsMessage = "";
		List<Vector3D> data = new LinkedList<>();

		for (int r = 1; r < 10; r++)
		{
			int attempts = 1000;
			ratio = r;
			int correct = 0;
			int noResult = 0;
			int bad = 0;
			for (int i = 0; i < attempts; i++)
			{
				data.clear();
				for (int d = 0; d < 1; d++)
				{
					loadTestData(data);
				}
				if (evaluateDataCombinations(data))
				{
					if (result > 39 && result < 46)
					{
						correct++;
					} else
					{
						bad++;
					}
				} else
				{
					noResult++;
				}
			}
			resultsMessage += correct + " bad " + bad + " ratio " + ratio + "\n";

		}
		System.out.println(resultsMessage);
	}

	private void loadTestData(List<Vector3D> data)
	{
		int scanSize = 78;
		int goodPoints = 25;

		int goodNoiseRange = 2;

		Random rand = new Random();

		for (int i = 0; i < scanSize - goodPoints; i++)
		{
			data.add(new Vector3D(rand.nextInt(150), rand.nextInt(150), 0));
		}

		for (int r = 0; r < goodPoints; r++)
		{
			int n1 = rand.nextInt(goodNoiseRange * 2) - goodNoiseRange;
			int n2 = rand.nextInt(goodNoiseRange * 2) - goodNoiseRange;
			data.add(rand.nextInt(data.size()), new Vector3D((r * 5) + n1, (r * 5) + n2, 0));
		}
	}

	@Override
	boolean evaluateSubset(List<Pair<Vector3D>> workingList)
	{

		// note this requires noise to work!!!

		Map<Integer, Integer> angleCount = new HashMap<>();
		for (Pair<Vector3D> v : workingList)
		{
			DataPair dataPair = (DataPair) v;
			if (!dataPair.v1.equals(dataPair.v2))
			{
				double distance = Vector3D.distance(dataPair.v1, dataPair.v2);
				if (distance > 10 && distance < 60)
				{
					int angle = (int) dataPair.getAngle();

					if (angle < 0)
					{
						angle += 360;
					}
					if (angle >= 180)
					{
						angle -= 180;
					}

					angle = angle / 5;
					angle = angle * 5;
					

					Integer count = angleCount.get(angle);
					if (count == null)
					{
						count = 0;
					}
					count++;
					angleCount.put(angle, count);
				}
			} else
			{
				// System.out.println("skipping " + dataPair.v1 + " " +
				// dataPair.v2);
			}
		}

		double max = 0;
		double total = 0;
		int angle = 0;
		for (Entry<Integer, Integer> entry : angleCount.entrySet())
		{
			Integer count = entry.getValue();
			if (count > max)
			{
				angle = entry.getKey();
			}
			max = Math.max(count, max);
			total += count;
		}
		if (total == 0)
		{
			return false;
		}
		// if (angle == 0)
		// {
		// System.out.println("HUH");
		// }
		double avg = total / angleCount.size();

		boolean success = max / avg > ratio;
		if (success)
		{
			System.out.println("" + workingList.size() + " " + avg + " " + max + " Success " + success + " "
					+ (max / avg) + " " + angle);
		}
		result = angle;
		return success;

	}

	class DataPair implements Pair<Vector3D>
	{

		private Vector3D v1;
		private Vector3D v2;

		public DataPair(Vector3D v1, Vector3D v2)
		{
			this.v1 = v1;
			this.v2 = v2;
		}

		double getAngle()
		{
			return Math.toDegrees(Math.atan2(v1.getY() - v2.getY(), v1.getX() - v2.getX()));
		}

	}

	@Override
	Pair<Vector3D> createPair(Vector3D v1, Vector3D v2)
	{
		return new DataPair(v1, v2);
	}

}
