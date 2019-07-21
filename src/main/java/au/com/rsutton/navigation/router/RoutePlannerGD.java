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
import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.units.DistanceUnit;

public class RoutePlannerGD implements DataSourceMap
{

	private double[] result = new double[] {
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	private List<Point> pointList = new CopyOnWriteArrayList<>();
	boolean buildPoints = false;

	public RoutePlannerGD()
	{

	}

	final Semaphore semaphore = new Semaphore(1);

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

			buildPoints = true;
			function.getValue(result);
			semaphore.release();
		}

	}

	GDFunction plan(double initialHeading, DistanceXY xy, List<Vector3D> vals)
	{

		System.out.println("initialHeading " + initialHeading);
		System.out.println("XY " + xy);
		System.out.println("Way Points " + vals);
		GDFunction function = getCostFunction(initialHeading, xy, vals, false);

		double[] params = new double[] {
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		// double straight = function.getValue(params);
		// params[0] = 45;
		// double left = function.getValue(params);
		// params[0] = -45;
		// double right = function.getValue(params);
		//
		// if (straight < left && straight < right)
		// {
		// params[0] = 0;
		// } else if (left < straight && left < right)
		// {
		// params[0] = 45;
		// } else
		// {
		// params[0] = -45;
		// }

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

				double lastParameter = 0;

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
