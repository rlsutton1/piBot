package au.com.rsutton.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import org.junit.Test;

import au.com.rsutton.mapping.particleFilter.KitchenMapBuilder;
import au.com.rsutton.mapping.probability.ProbabilityMap;

public class MainPanel extends JFrame implements Runnable
{

	private static final long serialVersionUID = -4490943128993707547L;

	private MapUI graph;

	static public void main(String[] args)
	{
		new MainPanel();

	}

	public MainPanel()
	{
		this.setBounds(0, 0, 850, 900);

		FlowLayout experimentLayout = new FlowLayout();

		this.setLayout(experimentLayout);

		graph = new MapUI();
		graph.setPreferredSize(new Dimension(750, 750));
		this.add(graph);

		setSize(700, 700);
		setLocation(200, 200);
		setVisible(true);

		new Thread(this, "ui").start();

	}

	public void addDataSource(PointSource map,Color color)
	{
		WrapperForMapInMapUI mapSource = new WrapperForMapInMapUI(map,color);
		graph.addDataSource(mapSource);

	}

	public void addDataSource(MapDataSource map)
	{
		graph.addDataSource(map);

	}
	
	public void addStatisticSource(StatisticSource source)
	{
		graph.addStatisticSource(source);
	}

	@Override
	public void run()
	{
		while (true)
		{
			graph.render(0, 0, 0.75);
			this.repaint();

			try
			{
				Thread.sleep(1000);
			} catch (InterruptedException e)
			{
				throw new RuntimeException("Exiting");
			}
		}
	}

}
