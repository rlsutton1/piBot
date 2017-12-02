package au.com.rsutton.hazelcast;

import java.awt.BasicStroke;
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

import au.com.rsutton.calabrate.EdgeHelper;
import au.com.rsutton.calabrate.Line;
import au.com.rsutton.calabrate.LineHelper;
import au.com.rsutton.robot.lidar.LidarObservation;

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
		int c = 0;
		for (LidarObservation point : laserData)
		{
			g2.setColor(new Color(0, 255, 0));
			c += 6;
			if (c > 512)
				c = 255;

			g2.drawRect((int) (point.getX() + centerX), (int) (point.getY() + centerY), 10, 10);
			points.add(point.getVector());

		}

		try
		{
			int color = 0;

			EdgeHelper edgeHelper = new EdgeHelper();
			if (points.size() > 2)
			{
				List<Line> lines = edgeHelper.getLines(points);

				g2.setStroke(new BasicStroke(3));
				// draw 4 point line segments
				for (Line linep : lines)
				{
					color++;
					List<Vector3D> line = linep.getRawPoints();
					for (int i = 1; i < line.size(); i++)
					{
						g2.setColor(new Color(255 * (color % 2), 0, 255 * ((color + 1) % 2)));

						g2.drawLine((int) line.get(i - 1).getX() + centerX, (int) line.get(i - 1).getY() + centerY,
								(int) line.get(i).getX() + centerX, (int) line.get(i).getY() + centerY);
					}
				}
				for (Line linep : lines)
				{
					color++;
					List<Vector3D> line = linep.getPoints();
					for (int i = 1; i < line.size(); i++)
					{
						if (linep.getR() > 0.7 && linep.getRawPoints().size() > 4)
						{
							g2.setColor(new Color(255, 255, 255));
						} else
						{
							g2.setColor(new Color(255, 0, 255));
						}
						g2.drawLine((int) line.get(i - 1).getX() + centerX, (int) line.get(i - 1).getY() + centerY,
								(int) line.get(i).getX() + centerX, (int) line.get(i).getY() + centerY);
					}
				}

				LineHelper lineHelper = new LineHelper();

				// // draw 4 point line segments
				// List<List<Vector3D>> lines =
				// lineHelper.scanForAndfindLines(points);
				// for (List<Vector3D> line : lines)
				// {
				// g2.drawLine((int) line.get(0).getX() + centerX, (int)
				// line.get(0).getY() + centerY,
				// (int) line.get(line.size() - 1).getX() + centerX, (int)
				// line.get(line.size() - 1).getY()
				// + centerY);
				// }

				// draw best matched agregated lines
				g2.setColor(new Color(0, 255, 255));

				List<Line> bestLines = lineHelper.getBestLine(points);
				for (Line bestLine : bestLines)
				{
					System.out.println("Line r " + bestLine.getR());
					List<Vector3D> line = bestLine.getPoints();
					g2.drawLine((int) line.get(0).getX() + centerX + 5, (int) line.get(0).getY() + centerY + 5,
							(int) line.get(line.size() - 1).getX() + centerX + 5, (int) line.get(line.size() - 1)
									.getY() + centerY + 5);
				}

			}
			g2.setStroke(new BasicStroke(1));

		} catch (IOException | InterruptedException | BrokenBarrierException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		currentImage.set(image);
	}

}