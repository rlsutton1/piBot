package au.com.rsutton.navigation.router;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.gradientdescent.GradientDescent;
import au.com.rsutton.gradientdescent.GradientDescent.GDFunction;
import au.com.rsutton.kalman.RobotPoseSourceTimeTraveling.RobotPoseInstant;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.units.DistanceUnit;

public class RoutePlannerGD implements DataSourceMap
{

	private double[] result = new double[] {
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	private List<Point> pointList = new CopyOnWriteArrayList<>();
	boolean buildPoints = false;

	volatile double angleToUse = 0;

	public RoutePlannerGD()
	{

	}

	final Semaphore semaphore = new Semaphore(1);
	private double lastUsedParameter;

	public void plan(RobotPoseInstant poseSource, RoutePlannerImpl planner)
	{
		if (semaphore.tryAcquire())
		{
			DistanceXY xy = poseSource.getXyPosition();

			ExpansionPoint next = new ExpansionPoint((int) xy.getX().convert(DistanceUnit.CM),
					(int) xy.getY().convert(DistanceUnit.CM), 0, null);

			List<Vector3D> vals = new LinkedList<>();

			int i = 1;
			int lookAheadCM = 75;
			int sampleDistance = 30;
			for (; i < lookAheadCM; i++)
			{
				next = planner.getRouteForLocation(next.getX(), next.getY());
				if (i % sampleDistance == 0)
				{
					vals.add(new Vector3D(next.getX(), next.getY(), 0));
				}
			}

			GDFunction function = plan(poseSource.getHeading(), xy, vals);

			// build list of point for rendering UI
			buildPoints = true;
			function.getValue(result);
			lastUsedParameter = result[0];
			angleToUse = getRadiusOfArc(result[0], 10);

			semaphore.release();
		}

	}

	ProbabilityMap makeWallCostMap(DistanceXY pos, int radius, ProbabilityMap world)
	{
		// set map to 0(clear) or INF(wall/unexplored)

		List<ExpansionPoint> expansionPoints = new LinkedList<>();

		int BLOCK_SIZE = 5;
		double EXISTANCE_THRESHOLD = 0.5;
		int WALL = 1000;

		ProbabilityMap wallCostMap = new ProbabilityMap(BLOCK_SIZE);
		wallCostMap.setDefaultValue(0.0);

		double x = pos.getX().convert(DistanceUnit.CM);
		double y = pos.getY().convert(DistanceUnit.CM);

		// copy a radius from the world map into the local map, marking occupied
		// locations to MAX_VALUE
		for (int dx = -radius; dx < radius; dx++)
		{
			for (int dy = -radius; dy < radius; dy++)
			{
				if (world.get(x + dx, y + dy) >= EXISTANCE_THRESHOLD)
				{
					wallCostMap.writeRadius((int) (x + dx), (int) (y + dy), Double.MAX_VALUE, 1);
				}
			}
		}

		for (int dx = -radius; dx < radius; dx++)
		{
			for (int dy = -radius; dy < radius; dy++)
			{
				if (world.get(x + dx, y + dy) < 1)
				{
					expansionPoints
							.add(new ExpansionPoint((int) (x + dx), (int) (y + dy), world.get(x + dx, y + dy), null));
				}
			}
		}

		while (!expansionPoints.isEmpty())
		{
			ExpansionPoint point = expansionPoints.get(0);
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

	public double getRadiusOfArc(double angle, double length)
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

	GDFunction plan(double initialHeading, DistanceXY xy, List<Vector3D> vals)
	{

		System.out.println("initialHeading " + initialHeading);
		System.out.println("XY " + xy);
		System.out.println("Way Points " + vals);
		GDFunction function = getCostFunction(initialHeading, xy, vals, false);

		double[] params = new double[] {
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		new GradientDescent(function, params).descend(0.001, 1);

		function = getCostFunction(initialHeading, xy, vals, true);

		result = new GradientDescent(function, params).descend(0.001, 1);

		return function;
	}

	private GDFunction getCostFunction(double initialHeading, DistanceXY xy, List<Vector3D> waypoints, boolean smooth)
	{
		GDFunction function = new GDFunction()
		{

			@Override
			public double getValue(double[] parameters)
			{
				if (buildPoints)
				{
					pointList.clear();
				}

				double[] bests = new double[waypoints.size()];
				for (int i = 0; i < bests.length; i++)
				{
					bests[i] = Double.MAX_VALUE;
				}

				Vector3D position = xy.getVector(DistanceUnit.CM);

				double score = 0;

				double heading = initialHeading;

				// start with the last used parameter, avoiding wild swings in
				// direction
				double lastParameter = lastUsedParameter;

				double pscore = 0;
				for (double parameter : parameters)
				{
					// penalize turning and change in turn
					pscore += Math.abs(parameter - lastParameter);
					// pscore += Math.abs(parameter) / 10.0;
					lastParameter = parameter;

					Vector3D v = new Vector3D(0, 5, 0);
					heading += parameter;
					Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(heading));
					v = rotation.applyTo(v);
					position = position.add(v);

					for (int i = 0; i < bests.length; i++)
					{
						double dist = position.distance(waypoints.get(i));
						if (dist < bests[i])
						{
							bests[i] = dist;
						}
					}

					if (buildPoints)
					{
						pointList.add(new Point((int) position.getX(), (int) position.getY()));
					}

				}
				for (int i = 0; i < bests.length; i++)
				{
					score += bests[i];
				}
				if (smooth)
				{
					score += pscore;
				}

				return score;
			}
		};
		return function;
	}

	@Override
	public List<Point> getPoints()
	{
		return pointList;
	}

	@Override
	public void drawPoint(BufferedImage image, double pointOriginX, double pointOriginY, double scale, double originalX,
			double originalY)
	{

		Graphics graphics = image.getGraphics();

		graphics.setColor(Color.ORANGE);

		graphics.drawLine((int) (pointOriginX), (int) (pointOriginY), (int) ((pointOriginX + 5)),
				(int) ((pointOriginY + 5)));
		// System.out.println(pointOriginX + " " + pointOriginY + " " + value);

	}

}
