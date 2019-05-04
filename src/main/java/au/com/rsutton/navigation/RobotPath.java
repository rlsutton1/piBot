package au.com.rsutton.navigation;

import java.util.LinkedList;
import java.util.List;

import au.com.rsutton.mapping.particleFilter.RobotPoseSource;
import au.com.rsutton.navigation.router.ExpansionPoint;
import au.com.rsutton.navigation.router.RoutePlanner;
import au.com.rsutton.units.DistanceUnit;

public class RobotPath
{

	List<ExpansionPoint> path = new LinkedList<>();

	RobotPath(RoutePlanner routePlanner, RobotPoseSource initialPosition)
	{
		int pfX = (int) initialPosition.getXyPosition().getX().convert(DistanceUnit.CM);
		int pfY = (int) initialPosition.getXyPosition().getY().convert(DistanceUnit.CM);
		if (routePlanner.hasPlannedRoute())
		{

			// get the path from the routePlanner
			ExpansionPoint next = routePlanner.getRouteForLocation(pfX, pfY);
			while (pfX != next.getX() || pfY != next.getY())
			{
				path.add(next);
				pfX = next.getX();
				pfY = next.getY();
				next = routePlanner.getRouteForLocation(pfX, pfY);
			}

		}

	}

	boolean isPathStillValid(LocationValidator validator)
	{

		for (ExpansionPoint position : path)
		{
			if (!validator.isLocationClear(position))
			{
				return false;
			}
		}

		return path.size() > 1;
	}

	/**
	 * if the robot is close enough to the current point it is removed and the
	 * next point is returned
	 * 
	 * @param point
	 * @return
	 */
	ExpansionPoint getNextPoint(RobotPoseSource position)
	{
		int pfX = (int) position.getXyPosition().getX().convert(DistanceUnit.CM);
		int pfY = (int) position.getXyPosition().getY().convert(DistanceUnit.CM);

		ExpansionPoint next = path.get(0);
		while (next.distanceTo(pfX, pfY) < 20 && !path.isEmpty())
		{
			path.remove(0);
			next = path.get(0);
		}

		return next;
	}

}
