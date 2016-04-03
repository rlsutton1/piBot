package au.com.rsutton.calabrate;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;

public class LineHelper
{

	public LineHelper() throws IOException, InterruptedException, BrokenBarrierException
	{

	}

	enum LinePointAnalysis
	{
		TO_SHORT, IS_A_LINE, TOO_FEW_POINTS, NOT_A_LINE
	}

	static final int MIN_POINTS = 4;
	static final int ACCURACY_MULTIPIER = 5;

//	public List<List<Vector3D>> scanForAndfindLines(List<Vector3D> points) throws InterruptedException,
//			BrokenBarrierException, IOException
//	{
//
//		// // find crisp edges in the point scan
//		// List<Integer> edges = findEdges(points, 30);
//		//
//		// // break the list of points up into segments divided by the crisp
//		// edges
//		// // to aid in accurately finding the ends of the lines
//		// List<List<Vector3D>> segments = new LinkedList<>();
//		//
//		// int start = 0;
//		// for (int i : edges)
//		// {
//		// segments.add(points.subList(start, i));
//		// start = i;
//		// }
//		// if (start < points.size() - 1)
//		// {
//		// segments.add(points.subList(start, points.size() - 1));
//		// }
//		//
//		// // iterate segements looking for lines
//		// List<List<Vector3D>> lines = new LinkedList<>();
//		// for (List<Vector3D> segment : segments)
//		// {
//		// lines.addAll(findLines(segment, 7));
//		// }
//		//
//		// //return lines;
//		return findPossibleLineSegments(points);
//	}


//	class LineSegment
//	{
//		List<Vector3D> points = new LinkedList<>();
//	}

	/**
	 * taken from
	 * http://stackoverflow.com/questions/849211/shortest-distance-between
	 * -a-point-and-a-line-segment
	 * 
	 * @param endA
	 * @param endB
	 * @param point
	 * @return
	 */
	double minDistBetweenPointAndLine(Vector3D endA, Vector3D endB, Vector3D point)
	{
		// v = endA
		// b = endB
		// p = point

		// Return minimum distance between line segment vw and point p
		// i.e. |w-v|^2 - avoid a sqrt
		double l2 = Math.pow(Vector3D.distance(endA, endB), 2);
		if (l2 == 0.0)
		{
			return Vector3D.distance(point, endA);
		}
		// v == w case
		// Consider the line extending the segment, parameterized as v + t (w -
		// v).
		// We find projection of point p onto the line.
		// It falls where t = [(p-v) . (w-v)] / |w-v|^2
		double t = Vector3D.dotProduct(point.subtract(endA), endB.subtract(endA)) / l2;
		if (t < 0.0)
		{
			// Beyond the 'v' end of the segment
			return Vector3D.distance(point, endA);
		} else if (t > 1.0)
		{
			// Beyond the 'w' end of the segment
			return Vector3D.distance(point, endB);
		}
		// Projection falls on the segment
		Vector3D projection = endA.add((endB.subtract(endA)).scalarMultiply(t));
		return Vector3D.distance(point, projection);
	}



	public List<Line> getBestLine(List<Vector3D> points)
	{
		int linesTokeep = 200;

		List<Line> bestLines = new LinkedList<>();
		for (int minPoints = 60; minPoints > 5; minPoints -= 1)
		{
			for (int i = 0; i < points.size(); i++)
			{
				List<Vector3D> segment = getArbitarySegment(i, points, minPoints);
				
				// System.out.println("R "+r);
				if (segment.size() > 5)
				{
					Line line = new Line( segment);


					if (line.getR() > 0.90)
					{

						bestLines.add(line);
						if (bestLines.size() > linesTokeep)
						{
							Collections.sort(bestLines);
							Line worst = bestLines.remove(0);
							Line best = bestLines.get(bestLines.size() - 1);
							if (worst.r > best.r)
							{
								System.out.println("Error, removing wrong end of list");
							}
						}
					}

				}
			}

		}

		mergeOverlappingLines(bestLines);

		return bestLines;
	}

	private void mergeOverlappingLines(List<Line> bestLines)
	{
		boolean merged = true;
		while (merged)
		{
			merged = false;
			for (Line line : bestLines)
			{

				for (Line line2 : bestLines)
				{
					if (line2 != line)
					{
						if (overlapsBy3Points(line.getRawPoints(), line2.getRawPoints()))
						{
							bestLines.remove(line2);
							Collections.replaceAll(bestLines, line, mergeLines(line, line2));
							merged = true;
							break;
						}
					}
				}
				if (merged)
				{
					break;
				}
			}
		}

	}

	private Line mergeLines(Line line, Line line2)
	{
		for (Vector3D point : line2.getRawPoints())
		{
			if (!line.getRawPoints().contains(point))
			{
				line.getRawPoints().add(point);
			}
		}
		return new Line( line.getRawPoints());

	}

	private boolean overlapsBy3Points(List<Vector3D> s1, List<Vector3D> s2)
	{
		int ctr = 0;
		for (Vector3D v1 : s1)
		{
			if (s2.contains(v1))
			{
				ctr++;
				if (ctr > 3)
				{
					return true;
				}
			}

		}
		return false;
	}



	private List<Vector3D> getArbitarySegment(int i, List<Vector3D> points, int minPoints)
	{
		List<Vector3D> newPoints = new LinkedList<>();
		for (int r = i; r < points.size(); r++)
		{
			newPoints.add(points.get(r));
			// we require at least 15 points in a line
			if (newPoints.size() > minPoints)
			{

				// check for irregular spacing of points
				Vector3D lastPoint = null;
				double min = 10000;
				double max = 0;
				double total = 0;
				double p2plength = 0;
				for (Vector3D point : newPoints)
				{
					if (lastPoint != null)
					{
						p2plength = Vector3D.distance(lastPoint, point);
						min = Math.min(min, p2plength);
						max = Math.max(max, p2plength);
						total += p2plength;
					}
					lastPoint = point;
				}
				double avg = total / newPoints.size();
				if (max > avg + 15 || min < avg - 15)
				{
					return new LinkedList<>();
				}

				// we require the line to be at least 50cm long
				double length = Vector3D.distance(newPoints.get(0), newPoints.get(newPoints.size() - 1));
				if (length > 25 && length < 200)
				{
					return newPoints;
				}

			}
		}
		return new LinkedList<>();

	}




	private List<Integer> findEdges(List<Vector3D> points, int edgeStepSize) throws InterruptedException,
			BrokenBarrierException, IOException
	{
		List<Integer> edges = new LinkedList<>();
		List<Integer> values = new LinkedList<>();
		int i = 0;
		for (Vector3D point : points)
		{
			int value = (int) Vector3D.distance(new Vector3D(0, 0, 0), point);
			values.add(value);
			if (values.size() > 6)
			{
				int av1 = (values.get(0) + values.get(1) + values.get(2)) / 3;
				int av2 = (values.get(3) + values.get(4) + values.get(5)) / 3;
				if (Math.abs(av1 - values.get(0)) < av1 * 0.1 && Math.abs(av2 - av1) > edgeStepSize)
				{
					System.out.println("Edge at " + i + " dist " + values.get(2));
					edges.add(i - 3);
				}

				values.remove(0);
			}
			i++;
		}
		return edges;

	}

}
