package au.com.rsutton.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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

	CoordinateClickListener clickListener;
	private double xOff;
	private double yOff;
	private double scale;

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.drawImage(currentImage.get(), 0, 0, this);

	}

	public void setCoordinateClickListener(CoordinateClickListener listener)
	{
		this.clickListener = listener;
		this.addMouseListener(new MouseListener()
		{

			@Override
			public void mouseReleased(MouseEvent arg0)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent arg0)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent arg0)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent arg0)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseClicked(MouseEvent arg0)
			{
				if (clickListener != null)
				{
					int x = (int) ((arg0.getX() - xOff) / scale);
					int y = (int) ((arg0.getY() - yOff) / scale);

					clickListener.clickAt(x, y);
					arg0.consume();
				}

			}
		});
	}

	public void addDataSource(DataSourceMap source)
	{
		sources.add(source);
	}

	Double lastYCenter = new Double(0);
	Double lastXCenter = new Double(0);;

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
		if (lastXCenter.isNaN() || lastXCenter.isInfinite())
		{
			lastXCenter = 0.0;
		}
		if (lastYCenter.isNaN() || lastYCenter.isInfinite())
		{
			lastYCenter = 0.0;
		}
		double xCenter = ((totalX / ctr2) * 0.25) + (lastXCenter * 0.75);
		double yCenter = ((totalY / ctr2) * 0.25) + (lastYCenter * 0.75);

		lastYCenter = yCenter;
		lastXCenter = xCenter;

		this.scale = scale;

		xOff = (((xOffset - xCenter) * scale)) + 350;
		yOff = (((yOffset - yCenter) * scale)) + 350;
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
