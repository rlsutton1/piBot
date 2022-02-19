package au.com.rsutton.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;

class WrapperForMapInMapUI implements DataSourceMap
{

	private DataSourcePoint map;

	Color color;

	public WrapperForMapInMapUI(DataSourcePoint map, Color color)
	{
		this.map = map;
		this.color = color;
	}

	@Override
	public List<Point> getPoints()
	{
		return map.getOccupiedPoints();
	}

	@Override
	public void drawPoint(BufferedImage image, double pointOriginX, double pointOriginY, double scale, double originalX,
			double originalY)
	{
		Graphics graphics = image.getGraphics();

		graphics.setColor(color);
		graphics.drawLine((int) pointOriginX, (int) pointOriginY, (int) pointOriginX + 1, (int) pointOriginY + 1);

	}
}
