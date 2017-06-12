package au.com.rsutton.mapping.particleFilter;

import java.util.LinkedList;
import java.util.List;

import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.navigation.router.ExpansionPoint;

public class Despecaler
{

	private Despecaler()
	{

	}

	public static void despecal(ProbabilityMapIIFc inputMap)
	{
		int minX = inputMap.getMinX();
		int maxX = inputMap.getMaxX();
		int minY = inputMap.getMinY();
		int maxY = inputMap.getMaxY();

		List<ExpansionPoint> toRemove = new LinkedList<>();

		for (int x = minX; x < maxX; x++)
		{
			for (int y = minY; y < maxY; y++)
			{
				if (inputMap.get(x, y) > 0.5)
				{
					int count = 0;
					for (ExpansionPoint point : getSouroundingList(inputMap.getBlockSize()))
					{
						if (inputMap.get(x + point.getX(), y + point.getY()) > 0.5)
						{
							count++;
						}
					}
					if (count < 1)
					{
						toRemove.add(new ExpansionPoint(x, y));
					}
				}
			}
		}

		for (ExpansionPoint point : toRemove)
		{

			inputMap.resetPoint(point.getX(), point.getY());
			// System.out.println("Removing point at " + point.getX() + "," +
			// point.getY());
		}

	}

	static List<ExpansionPoint> getSouroundingList(int radius)
	{
		List<ExpansionPoint> points = new LinkedList<>();
		points.add(new ExpansionPoint(radius, 0));
		points.add(new ExpansionPoint(radius, radius));
		points.add(new ExpansionPoint(radius, -radius));
		points.add(new ExpansionPoint(0, radius));
		points.add(new ExpansionPoint(0, -radius));
		points.add(new ExpansionPoint(-radius, 0));
		points.add(new ExpansionPoint(-radius, radius));
		points.add(new ExpansionPoint(-radius, -radius));
		return points;
	}
}
