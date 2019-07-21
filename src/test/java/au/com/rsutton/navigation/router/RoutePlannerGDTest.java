package au.com.rsutton.navigation.router;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;

import com.google.common.base.Stopwatch;

import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.units.DistanceUnit;

public class RoutePlannerGDTest
{

	@Test
	public void test()
	{

		for (int i = 0; i < 1000; i++)
		{
			Stopwatch timer = Stopwatch.createStarted();

			RoutePlannerGD planner = new RoutePlannerGD();

			List<Vector3D> vals = new LinkedList<>();
			vals.add(new Vector3D(60, 68, 0));
			vals.add(new Vector3D(83, 98, 0));

			DistanceXY xy = new DistanceXY(30, 43, DistanceUnit.CM);
			double initialHeading = 280;

			planner.plan(initialHeading, xy, vals);
			System.out.println("time " + timer.elapsedMillis());
		}
	}

}
