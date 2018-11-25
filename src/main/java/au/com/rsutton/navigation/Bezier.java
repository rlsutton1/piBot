package au.com.rsutton.navigation;

import java.awt.geom.Point2D;
import java.util.List;

public class Bezier
{

	public static Point2D parabolic2D(final List<Point2D> points, double position)
	{
		BezierList xList = new BezierList()
		{

			@Override
			public int size()
			{
				return points.size();
			}

			@Override
			public double get(int i)
			{
				return points.get(i).getX();
			}
		};

		BezierList yList = new BezierList()
		{

			@Override
			public int size()
			{
				return points.size();
			}

			@Override
			public double get(int i)
			{
				return points.get(i).getY();
			}
		};
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
		return new Point2D.Double(parabolic1D(xList, position), parabolic1D(yList, position));
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
}
