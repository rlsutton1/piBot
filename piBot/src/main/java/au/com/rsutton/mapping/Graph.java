package au.com.rsutton.mapping;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JPanel;

import au.com.rsutton.cv.CameraRangeData;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.hazelcast.RobotLocation;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.pi4j.gpio.extension.pixy.Coordinate;

public class Graph extends JPanel implements MessageListener<RobotLocation>
{

	private MapAccessor map;

	volatile int currentX = 0;
	volatile int currentY = 0;

	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		int h = getHeight();
		int w = getWidth();

		double minxy = Math.min(h, w);
		double offset = 1500;
		double scale = minxy / (offset * 2.0d);
		// Draw axeX.
		g2.draw(new Line2D.Double(0, (offset * scale), w, (offset * scale)));

		// to
		// make
		// axisX
		// in
		// the
		// middle
		// Draw axeY.
		g2.draw(new Line2D.Double((offset * scale), h, (offset * scale), 0));

		// to
		// make
		// axisY
		// in
		// the
		// middle of the panel

		if (lastHeading != null)
		{
			System.out.println(lastHeading);
			double xbot = (Math.sin(Math.toRadians(lastHeading)) * 40d);// +(Math.cos(Math.toRadians(lastHeading))*10);
			double ybot = -(Math.cos(Math.toRadians(lastHeading)) * 40d);// +(Math.sin(Math.toRadians(lastHeading))*10);

			g2.setColor(new Color(255, 0, 0));
			g2.draw(new Line2D.Double((offset * scale), (int) (offset * scale),
					(int) ((offset + xbot) * scale),
					(int) ((offset + ybot) * scale)));
			g2.setColor(new Color(0, 0, 0));
		}

		int blockSize = 20;

		for (int x = (int) -offset; x < offset; x += blockSize)
		{
			for (int y = (int) -offset; y < offset; y += blockSize)
			{
				if (!map.isMapLocationClear(x + currentX, -y + currentY,
						blockSize / 2))
				{
					int r = (int) Math.min(blockSize, (blockSize * scale) / 2);
					g.drawRect((int) ((x + offset) * scale) - r,
							(int) ((y + offset) * scale) - r, r * 2, r * 2);
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

		// Thread th = new Thread(graph);
		// th.start();
	}

	public Graph()
	{
		map = new MapAccessor();
		RobotLocation locationMessage = new RobotLocation();
		locationMessage.addMessageListener(this);

	}

	// @Override
	// public void run()
	// {
	// Random rand = new Random();
	// double domain = 600;
	// while (true)
	// {
	// this.repaint();
	// map.addObservation(new ObservationImpl(rand.nextDouble() * domain,
	// rand.nextDouble() * domain, rand.nextDouble()
	// * (domain / 3), LocationStatus.OCCUPIED));
	// try
	// {
	// Thread.sleep(100);
	// } catch (InterruptedException e)
	// {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	//
	// }

	volatile Double lastHeading = null;

	@Override
	public void onMessage(Message<RobotLocation> message)
	{
		RobotLocation messageObject = message.getMessageObject();
		if (messageObject.getCameraRangeData() != null)
		{
			int heading = messageObject.getHeading();

			lastHeading = (double) heading;

			currentX = (int) (messageObject.getX().convert(DistanceUnit.CM) * 1.25d);
			currentY = (int) (messageObject.getY().convert(DistanceUnit.CM) * 1.25d);

			double robotSinAngle = Math.sin(Math.toRadians(heading));
			double robotCosAngle = Math.cos(Math.toRadians(heading));

			CameraRangeData cameraRangeData = messageObject
					.getCameraRangeData();
			CoordResolver converter = new CoordResolver(
					cameraRangeData.getRangeFinderConfig());

			Collection<Coordinate> laserData = cameraRangeData.getRangeData();
			for (Coordinate vector : laserData)
			{

				XY xy = converter.convertImageXYtoAbsoluteXY(
						vector.getAverageX(), vector.getAverageY());

				xy = Translator2d.rotate(xy, heading);

				map.addObservation(new ObservationImpl(xy.getX() + currentX, xy
						.getY() + currentY, 1, LocationStatus.OCCUPIED));
			}

			this.repaint();

			// SetMotion message2 = new SetMotion();
			// message2.setSpeed(new Speed(new Distance(0, DistanceUnit.CM),
			// Time
			// .perSecond()));
			//
			// lastHeading += 2;
			// message2.setHeading(lastHeading);
			// message2.publish();
		}
	}
}