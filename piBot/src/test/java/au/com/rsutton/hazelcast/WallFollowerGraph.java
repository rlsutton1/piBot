package au.com.rsutton.hazelcast;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JPanel;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.calabrate.LineHelper;
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

		List<Vector3D> points = new LinkedList<>();
		for (LidarObservation point : laserData)
		{
			g2.drawRect((int) (point.getX() + centerX), (int) (point.getY() + centerY), 10, 10);
			points.add(point.getVector());

		}

		try
		{
			g2.setColor(new Color(255, 0, 0));
			LineHelper lineHelper = new LineHelper();

			List<List<Vector3D>> lines = lineHelper.scanForAndfindLines(points);
			for (List<Vector3D> line : lines)
			{
				g2.drawLine((int) line.get(0).getX()+ centerX, (int) line.get(0).getY()+ centerY, (int) line.get(line.size() - 1).getX()+ centerX,
						(int) line.get(line.size() - 1).getY()+ centerY);
			}
		} catch (IOException | InterruptedException | BrokenBarrierException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		currentImage.set(image);
	}

}