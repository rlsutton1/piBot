package au.com.rsutton.navigation.router.rrt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.google.common.base.Stopwatch;

import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;

public class TestRRT
{

	Pose2DWithConstraint start = new Pose2DWithConstraint(25, 5, 180.0d, false);
	Pose2DWithConstraint target = new Pose2DWithConstraint(20, 25, 45.0d, false);
	private BufferedImage image;
	private Visualization visualization;
	private Graphics2D graphics;
	private int width;
	private int height;

	@Test
	public void loop() throws Exception
	{

		createUI();
		String times = "";
		for (int i = 0; i < 100; i++)
		{
			Stopwatch timer = Stopwatch.createStarted();
			do
			{
				start = new Pose2DWithConstraint(5 + Math.random() * 20, 5 + Math.random() * 20, Math.random() * 360,
						false);
			} while (getMap().get((int) start.getX(), (int) start.getY()) != 0);

			do
			{
				target = new Pose2DWithConstraint(5 + Math.random() * 20, 5 + Math.random() * 20, Math.random() * 360,
						false);
			} while (getMap().get((int) target.getX(), (int) target.getY()) != 0);

			RrtBi<Pose2DWithConstraint> rrt = new RrtBi<>(start, target, getMap(), getNodeListener());
			// RRT<Pose2D> rrt = new RRT<>(new Pose2D(20, 5), new Pose2D(20,
			// 20),
			// getMap());
			RrtNode<Pose2DWithConstraint> path = rrt.solve(5, 10);
			times += "\n" + timer.elapsed(TimeUnit.SECONDS);

			for (int z = 0; z < 5; z++)
			{
				TimeUnit.SECONDS.sleep(3);

				do
				{
					start = new Pose2DWithConstraint(path.getPose().x + (Math.random() * 2) - 1,
							path.getPose().y + (Math.random() * 2) - 1,
							path.getPose().theta + (Math.random() * 2) - 179, false);
				} while (getMap().get((int) start.getX(), (int) start.getY()) != 0);

				timer = Stopwatch.createStarted();
				rrt.solveWithAlternateStartLocation(3, 1, start);

				times += "\nSolve with alternate start..." + timer.elapsed(TimeUnit.SECONDS);
				path = path.getParent();
			}
			TimeUnit.SECONDS.sleep(3);

		}
		System.out.println(times);
		TimeUnit.HOURS.sleep(1);
	}

	private void createUI()
	{

		width = 1200;
		height = 1200;

		visualization = new Visualization("title", 1000, 0, width, height);

	}

	private NodeListener<Pose2DWithConstraint> getNodeListener()
	{

		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		graphics = (Graphics2D) image.getGraphics();

		visualization.setImage(image);
		int scaleUp = 30;

		graphics.setColor(Color.GREEN);
		ProbabilityMapIIFc map = getMap();
		for (int x = 0; x < map.getMaxX(); x++)
		{
			for (int y = 0; y < map.getMaxY(); y++)
			{
				if (map.get(x, y) != 0)
					graphics.fillOval(((x * scaleUp)), ((y * scaleUp)), scaleUp, scaleUp);
			}
		}

		return new NodeListener<Pose2DWithConstraint>()
		{
			int steps = 0;

			@Override
			public void added(RrtNode<Pose2DWithConstraint> newNode, Color color, boolean forcePaint)
			{

				RrtNode<Pose2DWithConstraint> parent = newNode.getParent();
				double x1 = parent.getX();
				double y1 = parent.getY();
				double x2 = newNode.getX();
				double y2 = newNode.getY();

				graphics.setColor(color);
				if (color != Color.WHITE)
				{
					graphics.setStroke(new BasicStroke(scaleUp / 3));
					if (newNode.getPose().isReverse())
					{
						graphics.setColor(Color.RED);
					} else
					{
						graphics.setColor(Color.GREEN);
					}
				} else
				{
					graphics.setStroke(new BasicStroke(1));
				}

				graphics.drawLine((int) (x1 * scaleUp), (int) (y1 * scaleUp), (int) (x2 * scaleUp),
						(int) (y2 * scaleUp));

				graphics.setStroke(new BasicStroke(1));
				graphics.setColor(Color.MAGENTA);

				graphics.fillOval((int) ((start.getX() * scaleUp) - (0.5 * scaleUp)),
						(int) ((start.getY() * scaleUp) - (0.5 * scaleUp)), scaleUp, scaleUp);

				graphics.setColor(Color.RED);

				graphics.fillOval((int) ((target.getX() * scaleUp) - (0.5 * scaleUp)),
						(int) ((target.getY() * scaleUp) - (0.5 * scaleUp)), scaleUp, scaleUp);

				if (forcePaint || steps++ % 500 == 0 || steps < 1000)
				{
					visualization.repaint();
				}
				// try
				// {
				// TimeUnit.MILLISECONDS.sleep(1);
				// } catch (InterruptedException e)
				// {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
			}

		};
	}

	ProbabilityMapIIFc getMap()
	{
		Array2d<Integer> map = new Array2d<>(30, 30, new Integer[][] {
				{
						0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{
						0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{
						0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
				{
						0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
				{
						0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
				{
						0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
				{
						0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0 },
				{
						0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0 },
				{
						0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0 },
				{
						0, 0, 1, 1, 1, 1, 1, 1, 0, 1, 0, 0, 1, 1, 1, 0, 1, 1, 1, 0, 0 },
				{
						0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
				{
						0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
				{
						0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
				{
						0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
				{
						0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
				{
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{
						0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
				{
						0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
				{
						0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
				{
						0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
				{
						0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },

				{
						0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
				{
						0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
				{
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
				{
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
				{
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
				{
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 } },
				0);

		ProbabilityMap world = new ProbabilityMap(1, 0);
		for (int x = 0; x < map.getMaxX(); x++)
		{
			for (int y = 0; y < map.getMaxY(); y++)
			{
				world.setValue(x, y, map.get(x, y));
			}

		}

		return world;
	}
}
