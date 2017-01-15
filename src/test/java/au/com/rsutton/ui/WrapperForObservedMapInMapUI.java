package au.com.rsutton.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;

public class WrapperForObservedMapInMapUI implements DataSourceMap
{

	private ProbabilityMapIIFc world;

	public WrapperForObservedMapInMapUI(ProbabilityMapIIFc world)
	{
		this.world = world;
	}

	@Override
	public List<Point> getPoints()
	{
		List<Point> points = new LinkedList<>();
		for (int y = world.getMinY() - 3; y <= world.getMaxY(); y += 3)
		{
			for (int x = world.getMinX() - 3; x <= world.getMaxX(); x += 3)
			{
				points.add(new Point(x, y));
			}
		}
		return points;
	}

	@Override
	public void drawPoint(BufferedImage image, double pointOriginX, double pointOriginY, double scale)
	{
		Graphics graphics = image.getGraphics();

		double value = world.get((pointOriginX - 350) / scale, (pointOriginY - 350) / scale);

		Color color = new Color((int) (value * 255), (int) (value * 255), (int) (value * 255));

		graphics.setColor(color);
		graphics.drawLine((int) (pointOriginX), (int) (pointOriginY), (int) ((pointOriginX + 1)),
				(int) ((pointOriginY + 1)));
		// System.out.println(pointOriginX + " " + pointOriginY + " " + value);

	}
}
