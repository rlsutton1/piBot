package au.com.rsutton.navigation.router;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.kalman.RobotPoseSourceTimeTraveling.RobotPoseInstant;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.navigation.router.rrt.NodeListener;
import au.com.rsutton.navigation.router.rrt.Pose2DWithConstraint;
import au.com.rsutton.navigation.router.rrt.RrtBi;
import au.com.rsutton.navigation.router.rrt.RrtNode;
import au.com.rsutton.navigation.router.rrt.Visualization;
import au.com.rsutton.units.DistanceUnit;

public class RoutePlannerRRT
{

	volatile double angleToUse = 0;

	int width = 1200;
	int height = 1200;
	Visualization visualization = new Visualization("title", 1000, 0, width, height);
	private BufferedImage image;

	private Graphics2D graphics;
	Pose2DWithConstraint start = new Pose2DWithConstraint(25, 5, 180.0d, false);
	Pose2DWithConstraint target = new Pose2DWithConstraint(20, 25, 45.0d, false);

	private int scale;

	private int xoffset;

	private int yoffset;

	public RoutePlannerRRT()
	{

	}

	public int getScale()
	{
		return scale;
	}

	public int getXoffset()
	{
		return xoffset;
	}

	public int getYoffset()
	{
		return yoffset;
	}

	public RrtNode<Pose2DWithConstraint> plan(RobotPoseInstant poseSource, RoutePlanner planner,
			ProbabilityMapIIFc world)
	{

		DistanceXY xy = poseSource.getXyPosition();

		ExpansionPoint next = new ExpansionPoint((int) xy.getX().convert(DistanceUnit.CM),
				(int) xy.getY().convert(DistanceUnit.CM), 0, null);

		int i = 1;
		int lookAheadCM = 100;

		ExpansionPoint prev = null;

		for (; i < lookAheadCM; i++)
		{
			prev = next;
			next = planner.getRouteForLocation(next.getX(), next.getY());

		}
		double heading = Math.atan2(next.y - prev.y, next.x - prev.x);

		scale = 20;
		xoffset = -world.getMinX() / scale;
		yoffset = -world.getMinY() / scale;

		// convert world
		ProbabilityMapIIFc map = new ProbabilityMap(1, 1);
		for (int x = world.getMinX(); x < world.getMaxX(); x++)
		{
			for (int y = world.getMinY(); y < world.getMaxY(); y++)
			{
				if (world.get(x, y) < 0.5)
				{
					map.setValue((x / scale) + xoffset, (y / scale) + yoffset, 0);
				}
			}

		}

		start = new Pose2DWithConstraint((xy.getX().convert(DistanceUnit.CM) / scale) + xoffset,
				(xy.getY().convert(DistanceUnit.CM) / scale) + yoffset, -poseSource.getHeading(), false);

		target = new Pose2DWithConstraint((next.getX() / scale) + xoffset, (next.getY() / scale) + yoffset,
				180 - heading, false);

		RrtBi<Pose2DWithConstraint> rrt = new RrtBi<>(start, target, map, getNodeListener(map));
		// RRT<Pose2D> rrt = new RRT<>(new Pose2D(20, 5), new Pose2D(20,
		// 20),
		// getMap());
		return rrt.solve(5, 1);

	}

	private NodeListener<Pose2DWithConstraint> getNodeListener(ProbabilityMapIIFc map)
	{

		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		graphics = (Graphics2D) image.getGraphics();

		visualization.setImage(image);
		int scaleUp = 30;

		graphics.setColor(Color.GREEN);

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

	ProbabilityMap makeWallCostMap(DistanceXY pos, int radius, ProbabilityMapIIFc world)
	{
		// set map to 0(clear) or INF(wall/unexplored)

		List<ExpansionPoint> expansionPoints = new LinkedList<>();

		int BLOCK_SIZE = world.getBlockSize();
		double EXISTANCE_THRESHOLD = 0.5;
		int WALL = 1000;

		ProbabilityMap wallCostMap = new ProbabilityMap(BLOCK_SIZE, 0.0);

		double x = pos.getX().convert(DistanceUnit.CM);
		double y = pos.getY().convert(DistanceUnit.CM);

		// copy a radius from the world map into the local map, marking occupied
		// locations to MAX_VALUE
		for (int dx = -radius; dx < radius; dx += BLOCK_SIZE)
		{
			for (int dy = -radius; dy < radius; dy += BLOCK_SIZE)
			{
				if (world.get(x + dx, y + dy) >= EXISTANCE_THRESHOLD)
				{
					wallCostMap.writeRadius((int) (x + dx), (int) (y + dy), Double.MAX_VALUE, 1);
				}
			}
		}

		for (int dx = -radius; dx < radius; dx += BLOCK_SIZE)
		{
			for (int dy = -radius; dy < radius; dy += BLOCK_SIZE)
			{
				if (world.get(x + dx, y + dy) < 1)
				{
					expansionPoints
							.add(new ExpansionPoint((int) (x + dx), (int) (y + dy), world.get(x + dx, y + dy), null));
				}
			}
		}

		int ctr = 0;
		while (!expansionPoints.isEmpty())
		{
			ctr++;
			if (ctr % 1000 == 0)
				System.out.println("Size " + expansionPoints.size());
			ExpansionPoint point = expansionPoints.remove(0);
			for (ExpansionPoint neighbour : point.getNeighbours(BLOCK_SIZE))
			{
				double neighbourValue = wallCostMap.get(neighbour.getX(), neighbour.getY());
				if (point.getTotalCost() < neighbourValue)
				{
					if (point.getTotalCost() == 0)
					{
						if (neighbourValue > WALL)
						{
							wallCostMap.setValue(neighbour.getX(), neighbour.getY(), WALL);
							neighbour.setTotalCost((double) WALL);
							expansionPoints.add(neighbour);
						} else
						{
							// NO OP
						}
					} else if (neighbourValue > WALL)
					{
						wallCostMap.setValue(neighbour.getX(), neighbour.getY(), point.getTotalCost() + 1);
						neighbour.setTotalCost(point.getTotalCost() + 1);
						expansionPoints.add(neighbour);
					} else
					{
						// NO OP
					}
				}
			}

		}

		return wallCostMap;

	}

	double getRadius()
	{
		return angleToUse;
	}

	public static double getRadiusOfArc(double angle, double length)
	{
		// H is the hieght of the arc
		// W is the width of the arc

		// formula for the radius of an arc

		// r = (H/2)+ ((W^2)/8H)

		Vector3D v = new Vector3D(0, length, 0);

		Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(angle));

		Vector3D vector = v.add(rotation.applyTo(v));
		double w = vector.getNorm();
		double h = rotation.applyTo(v).getX();

		return (h / 2.0) + ((w * w) / (8.0 * h));

	}

}
