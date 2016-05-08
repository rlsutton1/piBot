package au.com.rsutton.mapping.v3;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.mapping.LocationStatus;
import au.com.rsutton.mapping.MapAccessor;
import au.com.rsutton.mapping.Observation;
import au.com.rsutton.mapping.ObservationImpl;
import au.com.rsutton.mapping.XY;
import au.com.rsutton.mapping.v3.impl.Feature;
import au.com.rsutton.mapping.v3.impl.ObservationOrigin;
import au.com.rsutton.mapping.v3.impl.ObservedPoint;
import au.com.rsutton.mapping.v3.linearEquasion.LinearEquasion;
import au.com.rsutton.mapping.v3.linearEquasion.LinearEquasionFactory;
import au.com.rsutton.robot.rover.Angle;
import au.com.rsutton.robot.rover.AngleUnits;

public class Mapography
{

	MapAccessor world = new MapAccessor();

	Observation makeObservationFromWorldMap(ObservationOrigin origin,
			Angle angle, Angle spread, Distance maxRange)
	{

		Set<XY> pointsToCheck = createArcPointSet(origin, angle, spread,
				maxRange);

		Observation point = null;
		for (XY xy : pointsToCheck)
		{
			List<Observation> values = getPointsInRange(new Rectangle(
					xy.getX(), xy.getY(), 1, 1));
			if (values.size() > 0)
			{
				point = values.get(0);
			}
		}

		return point;
	}

	/**
	 * create a set of xy coordinates for the indicated arc
	 * 
	 * @param origin
	 * @param angle
	 * @param spread
	 * @param maxRange
	 * @return
	 */
	Set<XY> createArcPointSet(ObservationOrigin origin, Angle angle,
			Angle spread, Distance maxRange)
	{
		Vector3D base = new Vector3D(origin.getLocation().getX(), origin
				.getLocation().getY());

		Set<XY> pointsToCheck = new HashSet<>();

		Map<Double, Rotation> rotationMap = new HashMap<>();

		double angleHalfStepSizeAtMaxRange = Math.sin(1.0 / maxRange
				.convert(DistanceUnit.CM)) / 2.0;

		double startAngle = angle.getRadians() - spread.getRadians();
		double endAngle = angle.getRadians() + spread.getRadians();
		for (double d = 0; d <= maxRange.convert(DistanceUnit.CM); d++)
		{
			for (double a = startAngle; a < endAngle; a += angleHalfStepSizeAtMaxRange)
			{
				Rotation rotation = rotationMap.get(a);
				if (rotation == null)
				{
					rotation = new Rotation(RotationOrder.XYZ, 0, 0, a);
					rotationMap.put(a, rotation);
				}
				Vector3D offset = new Vector3D(d, 0, 0);
				offset = rotation.applyTo(offset);
				offset = offset.add(base);
				pointsToCheck.add(new XY((int) offset.getX(), (int) offset
						.getY()));

			}
		}
		return pointsToCheck;
	}

	List<Observation> getPointsInRange(Rectangle rectangle)
	{
		return world.getPointsInRange(rectangle);
	}

	// void addPointIfMoreAccurate(ObservedPoint point);

	void addInitalScan(List<ObservedPoint> points)
	{

		for (ObservedPoint point : points)
		{
			world.addObservation(new ObservationImpl(point.getX(),
					point.getY(), 1, LocationStatus.OCCUPIED));
		}
	}

	/**
	 * 
	 * @param currentOrientation
	 * @param points
	 * @return corrected currentOrientation
	 * 
	 *         every point must have it's own ObservationOrigin, but they should
	 *         all be the same as currentOrientation.
	 * 
	 *         this method will add the points to the map and also return a
	 *         corrected ObservationOrigin as there will be error in the passed
	 *         in ObservationOrigin. this will allow correcting the robots
	 *         current location
	 */
	ObservationOrigin matchAndAddScan(ObservationOrigin currentOrientation,
			List<ObservedPoint> points)
	{
		List<LineSegmentData> scanFeatures = getLineFeaturesFromScan(points);

		// get equivilent scan from world map
		List<ObservedPoint> worldPoints = new LinkedList<>();
		for (ObservedPoint point : points)
		{
			Observation worldObservation = makeObservationFromWorldMap(
					currentOrientation, point.getObservedFrom()
							.getOrientation(), new Angle(1.8,
							AngleUnits.DEGREES), point.getDistance());
			if (worldObservation != null)
			{
				ObservedPoint wpoint = new ObservedPoint(new XY(
						(int) worldObservation.getX(),
						(int) worldObservation.getY()), currentOrientation,
						new Distance(1, DistanceUnit.CM));
				worldPoints.add(wpoint);
			}
		}
		List<LineSegmentData> worldFeatures = getLineFeaturesFromScan(worldPoints);

		// TODO: important stuff goes here

		// if compare angles of world lines to scan lines.. make adjustment

		// now compare distances to lines, and adjust location

		return new ObservationOrigin(null, null, null);
	}

	private List<LineSegmentData> getLineFeaturesFromScan(
			List<ObservedPoint> points)
	{

		double requiredAngleAccuracy = 1.8;

		// better than 1.8 degress
		double requiredErrorRatio = Math.sin(Math
				.toRadians(requiredAngleAccuracy));

		Map<Integer, LineSegmentData> linePositionAndSegmentData = generateShortLineSegmentList(
				points, requiredErrorRatio);

		List<LineSegmentData> lineList = getContinousLineSegments(
				requiredAngleAccuracy, linePositionAndSegmentData);

		return lineList;
	}

	private List<LineSegmentData> getContinousLineSegments(
			double requiredAngleAccuracy,
			Map<Integer, LineSegmentData> linePositionAndSegmentData)
	{
		List<LineSegmentData> lineList = new LinkedList<>();
		Integer lastPosition = null;
		LinearEquasion currentLineEquasion = null;
		ObservedPoint currentLineStartPoint = null;
		for (Entry<Integer, LineSegmentData> entry : linePositionAndSegmentData
				.entrySet())
		{
			LineSegmentData data = entry.getValue();
			Integer position = entry.getKey();
			if (currentLineEquasion != null)
			{
				// check for discontinuity
				boolean isLineDiscontinous = !currentLineEquasion.isSimilar(
						data.getAngle(), requiredAngleAccuracy, 1, new XY(
								(int) ((currentLineStartPoint.getX() + data
										.getSecondaryPoint().getX()) / 2),
								(int) ((currentLineStartPoint.getY() + data
										.getSecondaryPoint().getY()) / 2)));
				boolean isStepDiscontinous = lastPosition + 1 != position;

				if (isLineDiscontinous || isStepDiscontinous)
				{
					// add line
					lineList.add(new LineSegmentData(Vector3D.distance(
							currentLineStartPoint.getVector(), data
									.getSecondaryPoint().getVector()),
							currentLineStartPoint, data.getSecondaryPoint()));
					currentLineEquasion = null;

				}
			}
			if (currentLineEquasion == null)
			{
				// set the start point of the line
				currentLineEquasion = data.getAngle();
				currentLineStartPoint = data.getPrimaryPoint();
			}
			lastPosition = position;

		}
		return lineList;
	}

	private Map<Integer, LineSegmentData> generateShortLineSegmentList(
			List<ObservedPoint> points, double requiredErrorRatio)
	{
		Map<Integer, LineSegmentData> linePositionAndSegmentData = new HashMap<>();

		// interate point list
		int i = 0;
		for (ObservedPoint point : points)
		{
			double primaryPointAccuracy = point.getAccuracy();

			// look for the next point in the list that is far enough away to
			// conform to the accuracy requirements
			for (int r = i; r < points.size(); r++)
			{
				ObservedPoint secondaryPoint = points.get(r);
				double totalPointAccuracy = primaryPointAccuracy
						+ secondaryPoint.getAccuracy();
				double distance = Vector3D.distance(point.getVector(),
						secondaryPoint.getVector());
				if (totalPointAccuracy / distance < requiredErrorRatio)
				{
					// add the data of the conforming line to the result set

					linePositionAndSegmentData.put(i, new LineSegmentData(
							distance, point, secondaryPoint));
					break;
				}
			}
			i++;
		}
		return linePositionAndSegmentData;
	}

	private List<Feature> getCornerFeaturesFromScan(List<ObservedPoint> points)
	{

		List<ObservedPoint> workingList = new LinkedList<>();
		workingList.addAll(points);

		List<Feature> features = new LinkedList<>();
		// detect line over 4 points

		double lineErrorRatio = 0.1;

		for (int i = 0; workingList.size() >= 5; i++)
		{

			Vector3D p1 = new Vector3D(workingList.get(0).getX(), workingList
					.get(0).getY(), 0);
			Vector3D p2 = new Vector3D(workingList.get(1).getX(), workingList
					.get(1).getY(), 0);
			Vector3D p3 = new Vector3D(workingList.get(2).getX(), workingList
					.get(2).getY(), 0);
			Vector3D p4 = new Vector3D(workingList.get(3).getX(), workingList
					.get(3).getY(), 0);
			Vector3D p5 = new Vector3D(workingList.get(4).getX(), workingList
					.get(4).getY(), 0);

			double td = Vector3D.distance(p1, p4);
			double d1 = Vector3D.distance(p1, p2);
			double d2 = Vector3D.distance(p2, p3);
			double d3 = Vector3D.distance(p3, p4);
			if (Math.abs(td - (d1 + d2 + d3)) < lineErrorRatio * td)
			{
				// the first 4 points form a line

				double d4 = Vector3D.distance(p4, p5);
				double dn = Vector3D.distance(p1, p5);
				if (Math.abs(dn - (d1 + d2 + d3 + d4)) > lineErrorRatio * dn)
				{
					// the next point doesn't conform to the line... this is a
					// feature
					features.add(new Feature(new XY((int) ((p4.getX() + p5
							.getX()) / 2.0),
							(int) ((p4.getY() + p5.getY()) / 2.0))));
				}
			}
			workingList.remove(0);
		}
		return features;
	}

}
