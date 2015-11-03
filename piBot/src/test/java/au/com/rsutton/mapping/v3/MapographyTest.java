package au.com.rsutton.mapping.v3;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.mapping.XY;
import au.com.rsutton.mapping.v3.impl.ObservationOrigin;
import au.com.rsutton.robot.rover.Angle;
import au.com.rsutton.robot.rover.AngleUnits;

public class MapographyTest
{

	@Test
	public void test()
	{
		Mapography mapography = new Mapography();
		Angle spread = new Angle(5, AngleUnits.DEGREES);
		Angle angle = new Angle(180, AngleUnits.DEGREES);
		ObservationOrigin origin = new ObservationOrigin(new XY(0, 0), angle,
				spread);
		Distance distance = new Distance(50, DistanceUnit.CM);
		Set<XY> points = mapography.createArcPointSet(origin, angle, spread,
				distance);

		List<XY> sorted = new LinkedList<>();
		sorted.addAll(points);
		Collections.sort(sorted);
		for (XY xy : sorted)
		{
			System.out.println(xy.getX() + " " + xy.getY());
		}
	}

}
