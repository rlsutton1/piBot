package au.com.rsutton.mapping;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.hazelcast.SetMotion;

public class Graph extends JPanel implements Runnable,
		MessageListener<RobotLocation>
{

	private MapAccessor map;
	private int mheading = 0;

	protected void paintComponent(Graphics g)
	{
		int YP1, YP2;
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		int h = getHeight();
		int w = getWidth();

		double minxy = Math.min(h, w);
		double offset = 600;
		double scale = minxy / (offset * 2.0d);
		// Draw axeX.
		g2.draw(new Line2D.Double(0, (offset * scale), w, (offset * scale))); // to
																				// make
																				// axisX
																				// in
																				// the
		// middle
		// Draw axeY.
		g2.draw(new Line2D.Double((offset * scale), h, (offset * scale), 0));// to
																				// make
																				// axisY
																				// in
																				// the
		// middle of the panel
		
		int blockSize = 10;

		for (int x = (int) -offset; x < offset; x += blockSize)
		{
			for (int y = (int) -offset; y < offset; y += blockSize)
			{
				if (map.isMapLocationClear(x, y, blockSize/2) == LocationStatus.OCCUPIED)
				{
					int r = (int) Math.min(blockSize, (blockSize * scale) / 2);
					g.drawRect((int) ((x + offset) * scale) - r,
							(int) ((y + offset) * scale) - r, r*2, r*2);

				}
			}
		}

	}

	public static void main(String[] args)
	{
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Graph graph = new Graph();
		f.add(graph);
		f.setSize(400, 400);
		f.setLocation(200, 200);
		f.setVisible(true);

//		 Thread th = new Thread(graph);
//		 th.start();
	}

	Graph()
	{
		map = new MapAccessor();
		RobotLocation locationMessage = new RobotLocation();
		locationMessage.addMessageListener(this);

	}

	@Override
	public void run()
	{
		Random rand = new Random();
		double domain = 600;
		while (true)
		{
			this.repaint();
			map.addObservation(new ObservationImpl(rand.nextDouble() * domain,
					rand.nextDouble() * domain, rand.nextDouble()
							* (domain / 3), LocationStatus.OCCUPIED));
			try
			{
				Thread.sleep(100);
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public void onMessage(Message<RobotLocation> message)
	{
		int heading = message.getMessageObject().getHeading();
		Distance distance = message.getMessageObject().getClearSpaceAhead();

		double x = Math.sin(Math.toRadians(heading))
				* distance.convert(DistanceUnit.CM);
		double y = Math.cos(Math.toRadians(heading))
				* distance.convert(DistanceUnit.CM);

		map.addObservation(new ObservationImpl(x, y, 1, LocationStatus.OCCUPIED));
		this.repaint();

		SetMotion message2 = new SetMotion();
		message2.setSpeed(new Speed(new Distance(0, DistanceUnit.CM), Time
				.perSecond()));

		message2.setHeading((double) heading + 10);
		message2.publish();

	}
}