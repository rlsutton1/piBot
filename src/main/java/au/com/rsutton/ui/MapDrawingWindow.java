package au.com.rsutton.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import org.apache.commons.lang3.StringUtils;

public class MapDrawingWindow extends JFrame implements Runnable
{

	private static final long serialVersionUID = -4490943128993707547L;

	private MapUI graph;

	// static public void main(String[] args)
	// {
	// new MapDrawingWindow();
	//
	// }

	volatile boolean stop = false;

	private long refreshIntervalMs;

	public MapDrawingWindow(String string, int x, int y, long refreshIntervalMs, boolean autoScale)
	{
		this.refreshIntervalMs = refreshIntervalMs;
		if (StringUtils.isNotBlank(string))
		{
			setTitle(string);
		}
		this.setBounds(0, 0, 850, 900);

		FlowLayout experimentLayout = new FlowLayout();

		this.setLayout(experimentLayout);

		graph = new MapUI(autoScale);
		graph.setPreferredSize(new Dimension(750, 750));
		this.add(graph);

		setSize(600, 600);
		setLocation(x, y);
		setVisible(true);

		new Thread(this, "ui").start();

		addWindowListener(getWindowCloseListener());

	}

	public void setCoordinateClickListener(CoordinateClickListener listener)
	{
		graph.setCoordinateClickListener(listener);
	}

	public MapDrawingWindow(int x, int y, long refreshIntervalMs, boolean autoScale)
	{
		this("", x, y, refreshIntervalMs, autoScale);
	}

	public void addDataSource(DataSourcePoint map, Color color)
	{
		WrapperForMapInMapUI mapSource = new WrapperForMapInMapUI(map, color);
		graph.addDataSource(mapSource);

	}

	public void addDataSource(DataSourceMap map)
	{
		graph.addDataSource(map);

	}

	public void addStatisticSource(DataSourceStatistic source)
	{
		graph.addStatisticSource(source);
	}

	@Override
	public void run()
	{
		while (stop == false)
		{
			try
			{
				graph.render(0, 0, 0.5);
				this.repaint();
			} catch (Exception e)
			{
				e.printStackTrace();
			}

			try
			{
				Thread.sleep(refreshIntervalMs);
			} catch (InterruptedException e)
			{
				throw new RuntimeException("Exiting");
			}
		}
	}

	public void addDataSource(DataSourcePaintRegion dataSourcePaintRegion)
	{
		graph.addDataSource(dataSourcePaintRegion);

	}

	public void destroy()
	{
		stop = true;
		dispose();
	}

	private WindowListener getWindowCloseListener()
	{
		return new WindowAdapter()
		{

			@Override
			public void windowClosed(WindowEvent e)
			{
				stop = true;

			}

		};
	}
}
