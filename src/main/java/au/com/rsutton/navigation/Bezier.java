package au.com.rsutton.navigation;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.LinkedList;
import java.util.List;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;

public class Bezier
{

	private Double[] plist;

	public Bezier(Double[] ps)
	{
		this.plist = ps;
	}

	Double getPoint(double position)
	{
		return drawCurve(plist, position);
	}

	public static Point2D parabolic2D(final List<Point2D> points, double position)
	{

		if (points.size() == 2)
		{
			// simulate 3 points
			Point2D midPoint = new Point2D.Double((points.get(0).getX() + points.get(1).getX()) / 2.0,
					(points.get(0).getY() + points.get(1).getY()) / 2.0);
			points.add(1, midPoint);
		}
		if (points.size() < 3)
		{
			return new Point2D.Double(0.0, 0.0);
		}
		Bezier bezier = createBezier(points);
		return bezier.getPoint(position);

	}

	public static Bezier createBezier(final List<Point2D> points)
	{

		Double[] ps = new Double[points.size()];
		int i = 0;
		for (Point2D point : points)
		{
			ps[i++] = new Double(point.getX(), point.getY());
		}

		return new Bezier(ps);
	}

	interface BezierList
	{
		int size();

		double get(int i);
	}

	private static double parabolic1D(BezierList points, double position)
	{
		// there must be at least 3 points.

		double segments = points.size() - 2;

		int segment = (int) ((segments * position));

		double p1 = points.get(segment);
		double p2 = points.get(segment + 1);
		double p3 = points.get(segment + 2);

		double positionOffset = segment / segments;
		double positionLength = 1.0 / segments;

		double point2Scaler = (position - positionOffset) / positionLength;
		double point1Scaler = 1.0 - point2Scaler;

		double line1 = (p1 * point1Scaler) + (p2 * point2Scaler);
		double line2 = (p2 * point1Scaler) + (p3 * point2Scaler);

		double line1Scaler = 1.0 - position;
		double line2Scaler = position;
		return (line1 * line1Scaler) + (line2 * line2Scaler);

	}

	public double getRadiusAtPosition(double position, double step)
	{
		double h = step;

		Double p1 = getPoint(position);
		Double p2 = getPoint(position + h);
		double h1 = p2.getX() - p1.getX();
		double firstDeriv = DerivativeHelper.getDerivative(p1.getY(), p2.getY(), h1);

		Double p3 = getPoint(position + h + h);
		double h2 = p3.getX() - p2.getX();
		double secondDeriv = DerivativeHelper.getSecondDerivative(p1.getY(), p2.getY(), p3.getY(), h1, h2);

		double curvature = CurvatureToRadius.getCurvature(firstDeriv, secondDeriv);

		double angle1 = Math.atan2(p2.getY() - p1.getY(), p2.getX() - p1.getX());
		double angle2 = Math.atan2(p3.getY() - p2.getY(), p3.getX() - p2.getX());

		double direction = HeadingHelper.getChangeInHeading(Math.toDegrees(angle2), Math.toDegrees(angle1));

		return CurvatureToRadius.getRadius(curvature) * Math.signum(direction);
	}

	static List<List<Integer>> lut = new LinkedList<>();

	int binomial(int n, int k)
	{
		while (n >= lut.size())
		{
			int s = lut.size();
			List<Integer> nextRow = new LinkedList<>();
			nextRow.add(1);
			for (int i = 1, prev = s - 1; i < s; i++)
			{
				nextRow.add(lut.get(prev).get(i - 1) + lut.get(prev).get(i));
			}
			nextRow.add(1);
			lut.add(nextRow);
		}
		return lut.get(n).get(k);
	}

	int bezier(int n, int t)
	{
		int sum = 0;
		for (int k = 0; k <= n; k++)
			sum += binomial(n, k) * (1 - t) ^ (n - k) * t ^ (k);
		return sum;
	}

	/**
	 * sum += w[k] * binomial(n,k) * (1-t)^(n-k) * t^(k) return sum
	 **
	 * @param t
	 * @param w
	 * @return
	 */
	int bezier(BezierList w, double t)
	{
		int n = w.size();
		int sum = 0;
		for (int k = 0; k < n; k++)
			sum += w.get(k) * binomial(n, k) * Math.pow((1 - t), (n - k)) * Math.pow(t, (k));
		return sum;
	}

	Double drawCurve(Double[] points, double t)
	{
		if (points.length == 1)
		{
			return points[0];
		} else
		{
			Double[] newpoints = new Double[points.length - 1];
			for (int i = 0; i < newpoints.length; i++)
			{
				double x = (1 - t) * points[i].x + t * points[i + 1].x;
				double y = (1 - t) * points[i].y + t * points[i + 1].y;
				newpoints[i] = new Double(x, y);
			}
			return drawCurve(newpoints, t);
		}
	}
}
