package au.com.rsutton.entryPoint.trig;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;

public class TrigMathTest
{

	@Test
	public void testDistanceBetween()
	{
		// check zero distance
		Point p1 = new Point(new Distance(10, DistanceUnit.CM), new Distance(
				10, DistanceUnit.CM));
		Point p2 = new Point(new Distance(10, DistanceUnit.CM), new Distance(
				10, DistanceUnit.CM));
		assertTrue(TrigMath.distanceBetween(p1, p2).convert(DistanceUnit.CM) == 0);

		// check y
		p2 = new Point(new Distance(10, DistanceUnit.CM), new Distance(20,
				DistanceUnit.CM));
		assertTrue(TrigMath.distanceBetween(p1, p2).convert(DistanceUnit.CM) == 10);

		// check diagonal
		p2 = new Point(new Distance(20, DistanceUnit.CM), new Distance(20,
				DistanceUnit.CM));
		assertTrue(
				"" + TrigMath.distanceBetween(p1, p2).convert(DistanceUnit.CM),
				TrigMath.distanceBetween(p1, p2).convert(DistanceUnit.CM) > 14.0
						&& TrigMath.distanceBetween(p1, p2).convert(
								DistanceUnit.CM) < 14.3);

		// check negative distance
		p1 = new Point(new Distance(30, DistanceUnit.CM), new Distance(
				30, DistanceUnit.CM));

		assertTrue(
				"" + TrigMath.distanceBetween(p1, p2).convert(DistanceUnit.CM),
				TrigMath.distanceBetween(p1, p2).convert(DistanceUnit.CM) > 14.0
						&& TrigMath.distanceBetween(p1, p2).convert(
								DistanceUnit.CM) < 14.3);

		
		// check negative values
		p2 = new Point(new Distance(-20, DistanceUnit.CM), new Distance(-20,
				DistanceUnit.CM));
		p1 = new Point(new Distance(-30, DistanceUnit.CM), new Distance(
				-30, DistanceUnit.CM));

		assertTrue(
				"" + TrigMath.distanceBetween(p1, p2).convert(DistanceUnit.CM),
				TrigMath.distanceBetween(p1, p2).convert(DistanceUnit.CM) > 14.0
						&& TrigMath.distanceBetween(p1, p2).convert(
								DistanceUnit.CM) < 14.3);

	}

	@Test
	public void testPointsFormLine()
	{
		// test line
		Point p1 = new Point(new Distance(-0, DistanceUnit.CM), new Distance(-0,
				DistanceUnit.CM));
		Point p2 = new Point(new Distance(-10, DistanceUnit.CM), new Distance(
				-10, DistanceUnit.CM));
		Point p3 = new Point(new Distance(-20, DistanceUnit.CM), new Distance(-20,
				DistanceUnit.CM));

		assertTrue(TrigMath.pointsFormLine(p1, p2, p3, 0.01)==true);
		assertTrue(TrigMath.pointsFormLine(p1, p3, p2, 0.01)==true);
		assertTrue(TrigMath.pointsFormLine(p2, p1, p3, 0.01)==true);
		assertTrue(TrigMath.pointsFormLine(p2, p3, p1, 0.01)==true);
		assertTrue(TrigMath.pointsFormLine(p3, p1, p2, 0.01)==true);
		assertTrue(TrigMath.pointsFormLine(p3, p2, p1, 0.01)==true);
		// test non line
		p1 = new Point(new Distance(10, DistanceUnit.CM), new Distance(-0,
				DistanceUnit.CM));

		assertTrue(TrigMath.pointsFormLine(p1, p2, p3, 0.01)==false);

		// test near line
		p1 = new Point(new Distance(3, DistanceUnit.CM), new Distance(-0,
				DistanceUnit.CM));

		assertTrue(TrigMath.pointsFormLine(p1, p2, p3, 0.01)==true);

	}

}
