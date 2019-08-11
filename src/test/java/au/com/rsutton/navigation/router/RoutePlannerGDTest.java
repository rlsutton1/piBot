package au.com.rsutton.navigation.router;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;

import com.google.common.base.Stopwatch;

import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.units.DistanceUnit;

public class RoutePlannerGDTest
{

	// @Test
	// public void test()
	// {
	//
	// for (int i = 0; i < 1000; i++)
	// {
	// Stopwatch timer = Stopwatch.createStarted();
	//
	// RoutePlannerGD planner = new RoutePlannerGD();
	//
	// List<Vector3D> vals = new LinkedList<>();
	// vals.add(new Vector3D(60, 68, 0));
	// vals.add(new Vector3D(83, 98, 0));
	//
	// DistanceXY xy = new DistanceXY(30, 43, DistanceUnit.CM);
	// double initialHeading = 280;
	//
	// planner.plan(initialHeading, xy, vals, new ProbabilityMap(5));
	// System.out.println("time " + timer.elapsedMillis());
	// }
	// }

	// initialHeading 359.31
	// XY DistanceXY [x=Distance{value=-0.451868072442438, units=MM},
	// y=Distance{value=8.163148757026136, units=MM}]
	// Way Points [{4; -30; 0}, {-10; -60; 0}]

	@Test
	public void test2()
	{

		Stopwatch timer = Stopwatch.createStarted();

		RoutePlannerGD planner = new RoutePlannerGD();

		List<Vector3D> vals = new LinkedList<>();
		vals.add(new Vector3D(4, -30, 0));
		vals.add(new Vector3D(-10, -60, 0));

		DistanceXY xy = new DistanceXY(-0.045, -0.8, DistanceUnit.CM);
		double initialHeading = 359.31;

		planner.plan(initialHeading, xy, vals, new ProbabilityMap(5));
		System.out.println("time " + timer.elapsedMillis());

	}

}
