package au.com.rsutton.mapping;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFrame;
import javax.swing.JPanel;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.mapping.particleFilter.ScanObservation;
import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.navigation.feature.RobotLocationDeltaHelper;
import au.com.rsutton.navigation.feature.RobotLocationDeltaListener;
import au.com.rsutton.navigation.feature.RobotLocationDeltaMessagePump;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.AngleUnits;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;

public class Graph extends JPanel implements RobotLocationDeltaListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private MapAccessor map;

	volatile double currentX = 0;
	volatile double currentY = 0;

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.drawImage(currentImage.get(), 0, 0, this);

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
		new RobotLocationDeltaMessagePump(this);

	}

	volatile Double lastHeading = 0d;

	@Override
	public void onMessage(Angle deltaHeading, Distance deltaDistance, List<ScanObservation> robotLocation)
	{

		try
		{
			List<XY> translatedXyData = new LinkedList<>();

			List<XY> newPoints = new LinkedList<>(translatedXyData);

			lastHeading += HeadingHelper.getChangeInHeading(0, deltaHeading.getDegrees());
			DistanceXY position = RobotLocationDeltaHelper.applyDelta(deltaHeading, deltaDistance,
					new Angle(lastHeading, AngleUnits.DEGREES), new Distance(currentX, DistanceUnit.CM),
					new Distance(currentY, DistanceUnit.CM));

			currentX = position.getX().convert(DistanceUnit.CM);
			currentY = position.getY().convert(DistanceUnit.CM);

			System.out.println("XY " + currentX + " " + currentY + " " + lastHeading + " " + deltaHeading.getDegrees());

			for (ScanObservation vector : robotLocation)
			{

				XY xy = new XY(vector.getX() * 10, vector.getY() * 10);

				xy = Translator2d.rotate(xy, lastHeading);
				translatedXyData.add(xy);
				newPoints.add(xy);

				map.addObservation(
						new ObservationImpl(xy.getX() + currentX, xy.getY() + currentY, 1, LocationStatus.OCCUPIED));

			}
			renderMap(newPoints);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		this.repaint();

	}

	AtomicReference<BufferedImage> currentImage = new AtomicReference<>();

	void renderMap(List<XY> translatedXyData)
	{

		BufferedImage image = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = image.createGraphics();
		g2.setColor(new Color(255, 255, 255));

		int h = image.getHeight();
		int w = image.getWidth();

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
			double xbot = (Math.sin(Math.toRadians(lastHeading)) * 400d);// +(Math.cos(Math.toRadians(lastHeading))*10);
			double ybot = -(Math.cos(Math.toRadians(lastHeading)) * 400d);// +(Math.sin(Math.toRadians(lastHeading))*10);

			g2.setColor(new Color(255, 0, 0));
			g2.draw(new Line2D.Double((offset * scale), (int) (offset * scale), (int) ((offset + xbot) * scale),
					(int) ((offset + ybot) * scale)));

		}

		g2.setColor(new Color(255, 255, 255));

		int blockSize = 20;

		for (int x = (int) -offset; x < offset; x += blockSize)
		{
			for (int y = (int) -offset; y < offset; y += blockSize)
			{
				if (!map.isMapLocationClear((int) (x + currentX), (int) (-y + currentY), blockSize / 2))
				{
					int r = (int) Math.min(blockSize, (blockSize * scale) / 2);
					g2.drawRect((int) ((x + offset) * scale) - r, (int) ((y + offset) * scale) - r, r * 2, r * 2);

				}
			}
		}

		g2.setColor(new Color(255, 0, 0));
		for (XY xy : translatedXyData)
		{
			int r = (int) Math.min(blockSize, (blockSize * scale) / 2);
			g2.drawRect((int) ((xy.getX() + offset) * scale) - r, (int) ((-xy.getY() + offset) * scale) - r, r * 2,
					r * 2);
		}

		g2.setStroke(new BasicStroke(3));

		currentImage.set(image);
	}

	public void clearMap()
	{
		map.qt.clear();

	}

}