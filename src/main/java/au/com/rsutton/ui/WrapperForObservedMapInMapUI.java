package au.com.rsutton.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;

public class WrapperForObservedMapInMapUI implements DataSourceMap
{

	private ProbabilityMapIIFc world;

	public WrapperForObservedMapInMapUI(ProbabilityMapIIFc world)
	{
		this.world = world;
	}

	Map<Point, Double> pointValues = new HashMap<>();

	@Override
	public List<Point> getPoints()
	{
		List<Point> points = new LinkedList<>();
		for (int y = world.getMinY() - 30; y <= world.getMaxY() + 30; y += 3)
		{
			for (int x = world.getMinX() - 30; x <= world.getMaxX() + 30; x += 3)
			{
				Point point = new Point(x, y);
				points.add(point);

			}
		}
		return points;
	}

	@Override
	public void drawPoint(BufferedImage image, double pointOriginX, double pointOriginY, double scale, double originalX,
			double originalY)
	{
		Graphics graphics = image.getGraphics();

		double value = world.get(originalX, originalY);

		Color color = new Color((int) (value * 255), (int) (value * 255), (int) (value * 255));

		graphics.setColor(color);
		if (value > .998)
		{
			graphics.setColor(new Color(255, 0, 0));
		}
		if (value < 0.00001)
		{
			graphics.setColor(Color.YELLOW);

		}

		graphics.drawLine((int) (pointOriginX), (int) (pointOriginY), (int) ((pointOriginX + 1)),
				(int) ((pointOriginY + 1)));
		// System.out.println(pointOriginX + " " + pointOriginY + " " + value);

	}
}
