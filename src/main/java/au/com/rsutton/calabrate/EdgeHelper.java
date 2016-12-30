package au.com.rsutton.calabrate;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;

public class EdgeHelper
{

	public List<Line> getLines(List<Vector3D> points)
	{
		List<Line> result = new LinkedList<>();

		List<Vector3D> temp = new LinkedList<>();
		temp.addAll(points);

		result.add(new Line(temp));
		 result = breakOnIrregularSpacing(result);

		result = breakOnAngleChange(result);
		return result;

	}

	private List<Line> breakOnAngleChange(List<Line> lineList)
	{
		List<Line> result = new LinkedList<>();

		for (Line line : lineList)
		{

			List<Vector3D> points = line.getRawPoints();

			if (points.size() > 3)
			{
				int start = 0;
				double lastAngle = Math.toDegrees(Vector3D.angle(points.get(0), points.get(0).subtract(points.get(1))));
				Vector3D lastPoint = points.get(1);
				for (int i = 3; i < points.size(); i++)
				{
					// ensure minimum distance such that the angle is meaningful
					if (Vector3D.distance(lastPoint, points.get(i)) > 15)
					{
						double angle = Math.toDegrees(Vector3D.angle(lastPoint, lastPoint.subtract(points.get(i))));
						if (Math.abs(HeadingHelper.getChangeInHeading(lastAngle, angle)) > 30)
						{
							if (i > start + 2)
							{
								result.add(new Line(points.subList(start, i-2)));
							}
							start = i-2;
						}
						lastAngle = angle;
						lastPoint = points.get(i);
					}
				}
				if (start < points.size() - 1)
				{
					result.add(new Line(points.subList(start, points.size() - 1)));

				}
			}
		}
		return result;
	}

	private List<Line> breakOnIrregularSpacing(List<Line> lineList)
	{
		List<Line> result = new LinkedList<>();

		int maxSpacing = 15;

		for (Line line : lineList)
		{
			List<Vector3D> points = line.getRawPoints();
			int start = 0;
			Vector3D lastPoint = points.get(0);
			for (int i = 1; i < points.size(); i++)
			{
				double spacing = Vector3D.distance(lastPoint, points.get(i));
				if (Math.abs( spacing) > maxSpacing)
				{
					if (i > start+2 )
					{
						result.add(new Line(points.subList(start, i )));
					}
					start =  i;
				}
				
				lastPoint = points.get(i);
			}
			if (start < points.size() - 2)
			{
				result.add(new Line(points.subList(start, points.size() - 1)));

			}
		}
		return result;

	}
}
