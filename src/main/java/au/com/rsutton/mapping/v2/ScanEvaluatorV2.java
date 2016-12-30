package au.com.rsutton.mapping.v2;

import java.util.LinkedList;
import java.util.List;

import au.com.rsutton.entryPoint.trig.Point;
import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.mapping.XY;

public class ScanEvaluatorV2 implements ScanEvaluatorIfc
{
	/**
	 * takes a radial scan of the environment, and identifies lines and
	 * artifacts like corners
	 */

	double requiredLineQuality = 0.20;

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

		List<PointPairData> ppd = new LinkedList<>();
		for (int i = 0; i < scanPoints.size() - 3; i++)
		{
			PointPairData pointPairData = new PointPairData(scanPoints.get(i),
					scanPoints.get(i + 2));
			ppd.add(pointPairData);
		}

		List<Line> lines = new LinkedList<>();

		for (Line line : lines)
		{
			System.out.println(line);
		}
		return lines;
	}

	private void addLine(List<Line> lines, List<XY> selection, double variance)
	{

		// we have a line, so add it to the list and clear the
		// selection ready to find the next line!
		Line line = new Line(selection.get(0),
				selection.get(selection.size() - 1), variance, selection);
		lines.add(line);

	}

	class PointPairData 
	{
		double yIntercept;
		double angle;

		XY p1;
		XY p2;

		PointPairData(XY p1, XY p2)
		{
			double ratio = (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());

			angle = Math.toDegrees(Math.atan(ratio));

			// y=mx+c
			// y-mx = c;
			// c= y-mx;
			// p1.getY()=ratio*p1.getX()+c;
			//
			yIntercept = p1.getY() - (ratio * p1.getX());

		}

		
	}

	private Point getPointFromXY(XY xy)
	{
		return new Point(new Distance(xy.getX(), DistanceUnit.MM),
				new Distance(xy.getY(), DistanceUnit.MM));
	}
}
