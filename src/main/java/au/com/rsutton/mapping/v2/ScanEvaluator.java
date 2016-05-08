package au.com.rsutton.mapping.v2;

import java.util.LinkedList;
import java.util.List;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.entryPoint.trig.Point;
import au.com.rsutton.entryPoint.trig.TrigMath;
import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.mapping.XY;

public class ScanEvaluator implements ScanEvaluatorIfc
{
	/**
	 * takes a radial scan of the environment, and identifies lines and
	 * artifacts like corners
	 */

	/**
	 * line rules
	 * 
	 * find initial cluster of n points => seed line : cluster must be [scaler]
	 * * n times longer than wide : points must be from adjacent scan angles :
	 * apply pythag rule [overall length] = sum(lengths)* [pythagScaler]
	 * 
	 * extend seed line : new point must extend length of line along it's axis :
	 * new point must be from adjacent scan angles : new point must not deviate
	 * more than 20 degrees from the lines axis : line must still be [scaler] *
	 * [number of points] times longer that wide : apply pythag rule [overall
	 * length] = sum(lengths)* [pythagScaler]
	 */

	double requiredLineQuality = 2.00;

	/*
	 * (non-Javadoc)
	 * 
	 * @see au.com.rsutton.mapping.v2.ScanEvaluatorIfc#findLines(java.util.List)
	 */
	@Override
	public List<Line> findLines(List<XY> scanPoints)
	{

		System.out.println("Dumping line data for unit testing...");
		for (XY xy : scanPoints)
		{
			System.out.println("scanPoints.add(new XY(" + xy + "));");
		}
		System.out.println();
		List<Line> lines = new LinkedList<>();
		List<XY> selection = new LinkedList<>();
		while (scanPoints.size() > 0)
		{
			// add points to our selection
			selection.add(scanPoints.remove(0));
			if (selection.size() > 4)
			{
				// at least 5 points to consider a line
				double lineQuality = evaluate(selection);

				// a perfect line is 0

				if (meetsLineQualityCriteria(requiredLineQuality, lineQuality,
						selection) && scanPoints.size() == 0)
				{
					// end of data
					addLine(lines, selection, Math.abs(0.0d - lineQuality));
				} else if (false == meetsLineQualityCriteria(
						requiredLineQuality, lineQuality, selection))
				{
					if (selection.size() == 5)
					{
						// need more than five points to constitue a line, drop
						// the first
						// point and keep looking
						selection.remove(0);
					} else
					{
						// we had a line, now its turned to crap, probably a
						// corner or something!
						// remove the last point and add it back to the
						// scanPoints

						scanPoints.add(0,
								selection.remove(selection.size() - 1));
						addLine(lines, selection,
								Math.abs(0.0d - evaluate(selection)));
						selection.clear();
					}
				}
			}
		}
		// TODO: fix this...

		// straightenLines(lines);

		for (Line line : lines)
		{
			System.out.println(line);
		}
		return lines;
	}

	class LineGroup
	{
		double angle;
		List<Line> lines = new LinkedList<>();
	}

	void straightenLines(List<Line> lines)
	{
		List<LineGroup> groups = new LinkedList<>();
		LineGroup lineGroup = null;
		for (Line line : lines)
		{

			for (LineGroup group : groups)
			{
				if (Math.abs(HeadingHelper.getChangeInHeading(line.getAngle(),
						group.angle)) < 10.0d)
				{
					group.lines.add(line);
					lineGroup = group;
					break;
				}
			}
			if (lineGroup == null)
			{
				lineGroup = new LineGroup();
				lineGroup.angle = line.getAngle();
				lineGroup.lines.add(line);
				groups.add(lineGroup);
			}

			List<Double> angles = new LinkedList<>();
			for (Line groupLine : lineGroup.lines)
			{
				angles.add(groupLine.getAngle());
			}
			lineGroup.angle = averageAngles(angles);
		}
		for (LineGroup group : groups)
		{
			if (group.lines.size() > 0)
			{
				System.out.println("Real group");
			}
			for (Line line : group.lines)
			{
				double centerX = 0;
				double centerY = 0;
				for (XY point : line.getPoints())
				{
					centerX += point.getX();
					centerY += point.getY();
				}
				centerX /= line.getPoints().size();
				centerY /= line.getPoints().size();
				double length = TrigMath.distanceBetween(
						getPointFromXY(line.getStart()),
						getPointFromXY(line.getEnd())).convert(DistanceUnit.MM);
				double angle = group.angle;
				int xoffset = (int) ((Math.sin(Math.toRadians(angle)) * length) / 2.0d);
				int yoffset = (int) ((Math.cos(Math.toRadians(angle)) * length) / 2.0d);
				line.setStart(new XY((int) (centerX - xoffset),
						(int) (centerY + yoffset)));
				line.setEnd(new XY((int) (centerX + xoffset),
						(int) (centerY - yoffset)));

			}

		}

	}

	private double averageAngles(List<Double> angles)
	{
		return TrigMath.averageAngles(angles);
	}

	private boolean meetsLineQualityCriteria(double criteria,
			double lineQuality, List<XY> points)
	{
		int numberOfPoints = points.size();
		double absoluteQuality = Math.abs(0.0d - lineQuality);

		// three is the minimum number of points that constitute a line where
		// the straightness can be checked using pythagorus
		double scaling = 3.0d / ((double) numberOfPoints);

		return absoluteQuality < (criteria * scaling)
				&& checkPointDistribution(points);
	}

	private void addLine(List<Line> lines, List<XY> selection, double variance)
	{

		// we have a line, so add it to the list and clear the
		// selection ready to find the next line!
		Line line = new Line(selection.get(0),
				selection.get(selection.size() - 1), variance, selection);
		lines.add(line);

	}

	double evaluate(List<XY> scanPoints)
	{

		double maxDistance = 0;
		double minDistance = Double.MAX_VALUE;
		double distance = 0;
		Point head = getPointFromXY(scanPoints.get(0));
		Point lastPointProcessed = head;
		for (int i = 1; i < scanPoints.size(); i++)
		{
			Point currentPoint = getPointFromXY(scanPoints.get(i));
			double currentDistance = TrigMath.distanceBetween(
					lastPointProcessed, currentPoint).convert(DistanceUnit.MM);
			maxDistance = Math.max(maxDistance, currentDistance);

			minDistance = Math.min(minDistance, currentDistance);

			distance += currentDistance;
			lastPointProcessed = currentPoint;
		}
		double avgDistance = distance / (double) scanPoints.size();
		double tailToHead = TrigMath.distanceBetween(head, lastPointProcessed)
				.convert(DistanceUnit.MM);
		double error = Math.abs(tailToHead - distance) / tailToHead;

		if (maxDistance > avgDistance * 3 || minDistance < avgDistance / 3)
		{
			System.out.println("Rejected avg,max,min " + avgDistance + " "
					+ maxDistance + " " + minDistance);
			error = 1000;
		}
		return error;
	}

	/**
	 * ensure there are points above and below the line.
	 * 
	 * @param line
	 * @return
	 */
	boolean checkPointDistribution(List<XY> points)
	{

		// y = mx+c;
		// c = y-mx;

		XY start = points.get(0);
		XY end = points.get(points.size() - 1);
		double x = (start.getX() - end.getX());
		double y = (start.getY() - end.getY());

		double m = y / x;
		double c = start.getY() - (m * start.getX());

		int above = 0;
		int below = 0;
		for (XY p : points)
		{
			if (p.getY() - (m * p.getX()) > c)
			{
				above++;
			} else
			{
				below++;
			}
		}

		return above > 1 && below > 1;
	}

	private Point getPointFromXY(XY xy)
	{
		return new Point(new Distance(xy.getX(), DistanceUnit.MM),
				new Distance(xy.getY(), DistanceUnit.MM));
	}
}
