package au.com.rsutton.navigation.correction;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.mapping.particleFilter.ScanObservation;
import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.navigation.feature.RobotLocationDeltaHelper;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.AngleUnits;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;

public class HeadingCorrector
{

	double heading;
	Logger logger = LogManager.getLogger();
	protected Vector3D position = Vector3D.ZERO;

	private final static int REQUIRED_PLANE_LENGTH = 50;

	Plane referencePlane = null;

	class Plane
	{
		Angle direction;
		Vector3D position;
		double length;
		int seen;
	}

	public double getHeading()
	{
		return heading;
	}

	public void onMessage(Angle deltaHeading, Distance deltaDistance, List<ScanObservation> robotLocation)
	{
		logger.error("Delta heading " + deltaHeading.getDegrees());
		heading += deltaHeading.getDegrees();
		DistanceXY result = RobotLocationDeltaHelper.applyDelta(deltaHeading, deltaDistance,
				new Angle(heading, AngleUnits.DEGREES), new Distance(position.getX(), DistanceUnit.CM),
				new Distance(position.getY(), DistanceUnit.CM));

		position = result.getVector(DistanceUnit.CM);

		logger.error("Slam position " + deltaDistance + " " + position);

		List<Plane> planesInScan = findPlanes(resampleData(robotLocation));
		if (referencePlane == null && !planesInScan.isEmpty())
		{
			referencePlane = planesInScan.get(0);
			for (Plane plane : planesInScan)
			{
				if (referencePlane.length < plane.length)
				{
					referencePlane = plane;
				}

			}
		}
		correctHeading(planesInScan);

	}

	protected List<ScanObservation> resampleData(List<ScanObservation> lastObs2)
	{
		List<ScanObservation> ret = new LinkedList<>();
		if (!lastObs2.isEmpty())
		{
			ScanObservation last = lastObs2.get(0);
			ret.add(last);
			for (ScanObservation obs : lastObs2)
			{
				if (Vector3D.distance(last.getVector(), obs.getVector()) >= 10)
				{
					last = obs;
					ret.add(obs);
				}
			}
		}
		return ret;

	}

	private List<Plane> findPlanes(List<ScanObservation> robotLocation)
	{
		List<Plane> planes = new LinkedList<>();
		List<Double> angles = convertFromPointsToAngles(robotLocation);

		double lastAngle = 0;
		Integer start = null;
		int ctr = 0;
		for (Double angle : angles)
		{
			if (start == null)
			{
				lastAngle = angle;
				start = ctr;
			}
			if (Math.abs(lastAngle - angle) > 30)
			{
				if (ctr - start >= 5)
				{
					Plane plane = new Plane();
					ScanObservation p1 = robotLocation.get(start);
					ScanObservation p2 = robotLocation.get(ctr + 1);
					double dx = p1.getVector().getX() - p2.getVector().getX();
					double dy = p1.getVector().getY() - p2.getVector().getY();

					double length = Math.sqrt((dx * dx) + (dy * dy));
					if (length > REQUIRED_PLANE_LENGTH)
					{
						plane.direction = new Angle(Math.toDegrees(Math.atan2(dy, dx)) + heading + 90,
								AngleUnits.DEGREES);
						plane.position = new Vector3D(position.getX() + (dx / 2.0), position.getY() + (dy / 2.0), 0);
						plane.length = length;
						planes.add(plane);
					}
				}
				start = null;
			}
			ctr++;
		}

		return planes;
	}

	List<Double> convertFromPointsToAngles(List<ScanObservation> list)
	{
		List<Double> results = new LinkedList<>();

		for (int i = 1; i < list.size(); i++)
		{
			double deltaX = list.get(i - 1).getX() - list.get(i).getX();
			double deltaY = list.get(i - 1).getY() - list.get(i).getY();
			// double length = Vector3D.distance(list.get(i - 1),
			// list.get(i));
			double degrees = Math.toDegrees(Math.atan2(deltaY, deltaX));
			results.add(degrees);

		}
		return results;

	}

	private void correctHeading(List<Plane> planes)
	{
		for (Plane plane : planes)
		{
			if (referencePlane != null)
			{
				double adjustment = HeadingHelper.getChangeInHeading(plane.direction.getDegrees(),
						referencePlane.direction.getDegrees());
				while (adjustment > 45)
				{
					adjustment -= 90;
				}
				while (adjustment < -45)
				{
					adjustment += 90;
				}
				if (Math.abs(adjustment) < 25)
				{
					heading -= adjustment * 0.1;
					logger.error(
							"Corrected heading " + adjustment + " < _________-------------------------------------");
				} else
				{
					logger.error(
							"Suggested adjustment is " + adjustment + " ------------------------------------------");
				}
			}
		}

	}

}
