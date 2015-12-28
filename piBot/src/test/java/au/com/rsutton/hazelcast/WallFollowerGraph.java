package au.com.rsutton.hazelcast;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JPanel;

import au.com.rsutton.robot.rover.LidarObservation;

public class WallFollowerGraph extends JPanel
{

	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.drawImage(currentImage.get(), 0, 0, this);

	}

	AtomicReference<BufferedImage> currentImage = new AtomicReference<>();

	public void showPoints(Collection<LidarObservation> laserData)
	{

		BufferedImage image = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = image.createGraphics();
		g2.setColor(new Color(255, 255, 255));

		int h = image.getHeight();
		int w = image.getWidth();

		// Draw axeX.
		int centerY = h / 2;
		g2.draw(new Line2D.Double(0, centerY, w, centerY));
		int centerX = w / 2;
		g2.draw(new Line2D.Double(centerX, 0, centerX, h));

		g2.setColor(new Color(255, 255, 255));

		for (LidarObservation point : laserData)
		{
			g2.drawRect((int)(point.getX()+centerX), (int) (point.getY()+centerY), 10, 10);

		}

		currentImage.set(image);
	}

}