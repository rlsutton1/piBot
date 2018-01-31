package au.com.rsutton.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JPanel;

public class MapUI extends JPanel
{

	private List<DataSourceMap> sources = new CopyOnWriteArrayList<>();
	private List<DataSourceStatistic> statisticSources = new CopyOnWriteArrayList<>();
	private List<DataSourcePaintRegion> paintSources = new CopyOnWriteArrayList<>();

	AtomicReference<BufferedImage> currentImage = new AtomicReference<>();

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.drawImage(currentImage.get(), 0, 0, this);

	}

	public void addDataSource(DataSourceMap source)
	{
		sources.add(source);
	}

	public void render(double xOffset, double yOffset, double scale)
	{

		double totalX = 0;
		double totalY = 0;
		double ctr2 = 0;
		for (DataSourceMap source : sources)
		{
			for (Point point : source.getPoints())
			{
				totalX += point.getX();
				totalY += point.getY();
				ctr2++;

			}
		}

		double xCenter = totalX / ctr2;
		double yCenter = totalY / ctr2;

		double xOff = (((xOffset - xCenter) * scale)) + 350;
		double yOff = (((yOffset - yCenter) * scale)) + 350;
		BufferedImage image = new BufferedImage(700, 700, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = image.createGraphics();
		g2.setColor(new Color(255, 255, 255));

		for (DataSourceMap source : sources)
		{
			for (Point point : source.getPoints())
			{
				source.drawPoint(image, xOff + ((point.getX()) * scale), (yOff + point.getY() * scale), scale,
						point.getX(), point.getY());
			}
		}

		g2.setColor(new Color(255, 255, 255));
		int ctr = 0;
		for (DataSourceStatistic stat : statisticSources)
		{
			ctr++;
			String label = stat.getLabel();
			String value = stat.getValue();
			g2.drawString(label, 40, ctr * 15);
			g2.drawString(value, 200, ctr * 15);
		}

		for (DataSourcePaintRegion paintsource : paintSources)
		{
			paintsource.paint(g2);
		}

		currentImage.set(image);

	}

	public void addStatisticSource(DataSourceStatistic source)
	{
		statisticSources.add(source);

	}

	public void addDataSource(DataSourcePaintRegion dataSourcePaintRegion)
	{
		paintSources.add(dataSourcePaintRegion);

	}

}
