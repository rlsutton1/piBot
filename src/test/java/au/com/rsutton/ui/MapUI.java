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

	private List<MapDataSource> sources= new CopyOnWriteArrayList<>();
	private List<StatisticSource> statisticSources= new CopyOnWriteArrayList<>();
	AtomicReference<BufferedImage> currentImage = new AtomicReference<>();
	
	

	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.drawImage(currentImage.get(), 0, 0, this);

	}
	
	public void addDataSource(MapDataSource source)
	{
		sources.add(source);
	}
	
	public void render(double xOffset, double yOffset, double scale)
	{
		
		double xOff = (xOffset*scale)+350;
		double yOff = (yOffset*scale)+350;
		BufferedImage image = new BufferedImage(700, 700, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = image.createGraphics();
		g2.setColor(new Color(255, 255, 255));

		for (MapDataSource source:sources)
		{
			for (Point point: source.getPoints())
			{
				source.drawPoint(image,xOff+(point.getX()*scale),(yOff+point.getY()*scale),scale);
			}
		}
		
		g2.setColor(new Color(255, 255, 255));
		int ctr = 0;
		for (StatisticSource stat:statisticSources)
		{
			ctr++;
			String label = stat.getLabel();
			String value = stat.getValue();
			g2.drawString(label, 40, ctr*15);
			g2.drawString(value, 200, ctr*15);
		}
		
		
		currentImage.set(image);
	
		
	}

	public void addStatisticSource(StatisticSource source)
	{
		statisticSources.add(source);
		
	}
	
}
