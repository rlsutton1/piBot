package au.com.rsutton.navigation;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.mapping.XY;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;

public class LastMeterPlanner
{

	class PathPoint
	{
		public PathPoint(XY xy)
		{
			source = xy;
		}

		XY source;
		XY selected;
		Vector3D angle;
		Vector3D limit1;
		Vector3D limit2;
	}

	List<XY> getPath(List<XY> points, ProbabilityMapIIFc worldMap)
	{
		List<PathPoint> working = new LinkedList<>();
		for (XY xy : points)
		{
			working.add(new PathPoint(xy));
		}

		determineAngles(working);

		ProbabilityMapIIFc map = makeMap(worldMap);
		determineLimits(working, map);
		makeSelection(working);

		List<XY> path = new LinkedList<>();
		for (PathPoint point : working)
		{
			path.add(point.selected);
		}

		return path;

	}

	private ProbabilityMapIIFc makeMap(ProbabilityMapIIFc worldMap)
	{
		// need to add the current scan to this
		return worldMap;
	}

	private void makeSelection(List<PathPoint> working)
	{
		// first attempt, just use the average of the limits, we can do
		// something smarter later
		for (PathPoint point : working)
		{
			point.selected = new XY((int) ((point.limit1.getX() + point.limit2.getX()) / 2.0),
					(int) ((point.limit1.getY() + point.limit2.getY()) / 2.0));
		}

	}

	private void determineLimits(List<PathPoint> working, ProbabilityMapIIFc map)
	{
		for (PathPoint point : working)
		{
			Vector3D v = point.angle.normalize();
			for (int i = 0; i < 50; i += 1)
			{
				Vector3D p1 = new Vector3D(point.source.getX(), point.source.getY(), 0).subtract(v.scalarMultiply(i));
				if (point.limit1 == null && map.get(p1.getX(), p1.getY()) >= 0.5)
				{
					point.limit1 = p1;
				}
				Vector3D p2 = new Vector3D(point.source.getX(), point.source.getY(), 0).add(v.scalarMultiply(i));
				if (point.limit2 == null && map.get(p2.getX(), p2.getY()) >= 0.5)
				{
					point.limit2 = p2;
				}
			}
			if (point.limit1 == null)
			{
				point.limit1 = new Vector3D(point.source.getX(), point.source.getY(), 0).subtract(v.scalarMultiply(50));

			}
			if (point.limit2 == null)
			{
				point.limit2 = new Vector3D(point.source.getX(), point.source.getY(), 0).add(v.scalarMultiply(50));
			}
		}
	}

	private void determineAngles(List<PathPoint> working)
	{
		for (int i = 0; i < working.size(); i++)
		{
			PathPoint self = working.get(i);
			Vector3D v1 = null;
			Vector3D v2 = null;
			if (i > 0)
			{
				PathPoint prev = working.get(i - 1);
				v1 = new Vector3D(self.source.getX() - prev.source.getX(), self.source.getY() - prev.source.getY(), 0);
			}

			if (i < working.size() - 1)
			{
				PathPoint next = working.get(i + 1);
				v2 = new Vector3D(next.source.getX() - self.source.getX(), next.source.getY() - self.source.getY(), 0);
			}
			Rotation rotate = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(90));
			if (v1 != null && v2 != null)
			{
				if (v1.equals(v2))
				{
					self.angle = rotate.applyTo(v2);
				} else
				{
					self.angle = rotate.applyTo(v1.subtract(v2));
				}
			} else if (v2 != null)
			{
				self.angle = rotate.applyTo(v2);
			} else if (v1 != null)
			{
				self.angle = rotate.applyTo(v1);
			}

			if (Vector3D.ZERO.equals(self.angle))
			{
				System.out.println("no nos lskdflj");
			}

		}

	}
}
