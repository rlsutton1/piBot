package au.com.rsutton.mapping.v3.linearEquasion;

import static org.junit.Assert.*;

import org.junit.Test;

import au.com.rsutton.mapping.XY;
import au.com.rsutton.mapping.v3.impl.ObservedPoint;

public class LinearEquasionFactoryTest
{

	@Test
	public void testHorizontalAndOblique()
	{

		ObservedPoint p1 = new ObservedPoint(new XY(0, 0), null, null);
		ObservedPoint p2 = new ObservedPoint(new XY(10, 0), null, null);
		LinearEquasion eq1 = LinearEquasionFactory.getEquasion(p1, p2);

		assertTrue(eq1 instanceof HorizontalLine);

		ObservedPoint p3 = new ObservedPoint(new XY(0, 0), null, null);
		ObservedPoint p4 = new ObservedPoint(new XY(1, 1), null, null);
		LinearEquasion eq2 = LinearEquasionFactory.getEquasion(p3, p4);

		InterceptResult r1 = eq1.getIntercept(eq2);

		InterceptResult r2 = eq2.getIntercept(eq1);

		assertTrue(r1.type == InterceptType.INTERCEPT);
		assertTrue(r1.location.equals(new XY(0, 0)));
		assertTrue(r2.type == InterceptType.INTERCEPT);
		assertTrue(r2.location.equals(new XY(0, 0)));

	}

	@Test
	public void testVerticalAndOblique()
	{

		ObservedPoint p1 = new ObservedPoint(new XY(0, 0), null, null);
		ObservedPoint p2 = new ObservedPoint(new XY(0, 10), null, null);
		LinearEquasion eq1 = LinearEquasionFactory.getEquasion(p1, p2);

		assertTrue(eq1 instanceof VerticalLine);

		ObservedPoint p3 = new ObservedPoint(new XY(0, 0), null, null);
		ObservedPoint p4 = new ObservedPoint(new XY(1, 1), null, null);
		LinearEquasion eq2 = LinearEquasionFactory.getEquasion(p3, p4);

		InterceptResult r1 = eq1.getIntercept(eq2);

		InterceptResult r2 = eq2.getIntercept(eq1);

		assertTrue(r1.type == InterceptType.INTERCEPT);
		assertTrue(r1.location.equals(new XY(0, 0)));
		assertTrue(r2.type == InterceptType.INTERCEPT);
		assertTrue(r2.location.equals(new XY(0, 0)));

	}

	@Test
	public void testSameHorizontalLine()
	{

		ObservedPoint p1 = new ObservedPoint(new XY(0, 0), null, null);
		ObservedPoint p2 = new ObservedPoint(new XY(10, 0), null, null);
		LinearEquasion eq1 = LinearEquasionFactory.getEquasion(p1, p2);

		assertTrue(eq1 instanceof HorizontalLine);

		ObservedPoint p3 = new ObservedPoint(new XY(0, 0), null, null);
		ObservedPoint p4 = new ObservedPoint(new XY(10, 0), null, null);
		LinearEquasion eq2 = LinearEquasionFactory.getEquasion(p3, p4);

		InterceptResult r1 = eq1.getIntercept(eq2);

		InterceptResult r2 = eq2.getIntercept(eq1);

		assertTrue(r1.type == InterceptType.SAME_LINE);
		// assertTrue(r1.location.equals( new XY(0,0)));
		assertTrue(r2.type == InterceptType.SAME_LINE);
		// assertTrue(r2.location.equals( new XY(0,0)));

	}

	@Test
	public void testSameVerticalLine()
	{

		ObservedPoint p1 = new ObservedPoint(new XY(0, 0), null, null);
		ObservedPoint p2 = new ObservedPoint(new XY(0, 10), null, null);
		LinearEquasion eq1 = LinearEquasionFactory.getEquasion(p1, p2);

		assertTrue(eq1 instanceof VerticalLine);

		ObservedPoint p3 = new ObservedPoint(new XY(0, 0), null, null);
		ObservedPoint p4 = new ObservedPoint(new XY(0, 10), null, null);
		LinearEquasion eq2 = LinearEquasionFactory.getEquasion(p3, p4);

		InterceptResult r1 = eq1.getIntercept(eq2);

		InterceptResult r2 = eq2.getIntercept(eq1);

		assertTrue(r1.type == InterceptType.SAME_LINE);
		// assertTrue(r1.location.equals( new XY(0,0)));
		assertTrue(r2.type == InterceptType.SAME_LINE);
		// assertTrue(r2.location.equals( new XY(0,0)));

	}

	@Test
	public void testSameObliqueLine()
	{

		ObservedPoint p1 = new ObservedPoint(new XY(0, 0), null, null);
		ObservedPoint p2 = new ObservedPoint(new XY(10, 10), null, null);
		LinearEquasion eq1 = LinearEquasionFactory.getEquasion(p1, p2);

		assertTrue(eq1 instanceof LinearEquasionNormal);

		ObservedPoint p3 = new ObservedPoint(new XY(0, 0), null, null);
		ObservedPoint p4 = new ObservedPoint(new XY(10, 10), null, null);
		LinearEquasion eq2 = LinearEquasionFactory.getEquasion(p3, p4);

		InterceptResult r1 = eq1.getIntercept(eq2);

		InterceptResult r2 = eq2.getIntercept(eq1);

		assertTrue(r1.type == InterceptType.SAME_LINE);
		// assertTrue(r1.location.equals( new XY(0,0)));
		assertTrue(r2.type == InterceptType.SAME_LINE);
		// assertTrue(r2.location.equals( new XY(0,0)));

	}

	@Test
	public void testOppositeObliqueLine()
	{

		ObservedPoint p1 = new ObservedPoint(new XY(0, 0), null, null);
		ObservedPoint p2 = new ObservedPoint(new XY(10, 10), null, null);
		LinearEquasion eq1 = LinearEquasionFactory.getEquasion(p1, p2);

		assertTrue(eq1 instanceof LinearEquasionNormal);

		ObservedPoint p3 = new ObservedPoint(new XY(0, 0), null, null);
		ObservedPoint p4 = new ObservedPoint(new XY(10, -10), null, null);
		LinearEquasion eq2 = LinearEquasionFactory.getEquasion(p3, p4);

		InterceptResult r1 = eq1.getIntercept(eq2);

		InterceptResult r2 = eq2.getIntercept(eq1);

		assertTrue(r1.type == InterceptType.INTERCEPT);
		assertTrue(r1.location.equals(new XY(0, 0)));
		assertTrue(r2.type == InterceptType.INTERCEPT);
		assertTrue(r2.location.equals(new XY(0, 0)));

	}

	@Test
	public void testParrelleObliqueLine()
	{

		ObservedPoint p1 = new ObservedPoint(new XY(0, 0), null, null);
		ObservedPoint p2 = new ObservedPoint(new XY(10, 10), null, null);
		LinearEquasion eq1 = LinearEquasionFactory.getEquasion(p1, p2);

		assertTrue(eq1 instanceof LinearEquasionNormal);

		ObservedPoint p3 = new ObservedPoint(new XY(0, 10), null, null);
		ObservedPoint p4 = new ObservedPoint(new XY(10, 20), null, null);
		LinearEquasion eq2 = LinearEquasionFactory.getEquasion(p3, p4);

		InterceptResult r1 = eq1.getIntercept(eq2);

		InterceptResult r2 = eq2.getIntercept(eq1);

		assertTrue(r1.type == InterceptType.NONE);
		//assertTrue(r1.location.equals(new XY(0, 0)));
		assertTrue(r2.type == InterceptType.NONE);
		//assertTrue(r2.location.equals(new XY(0, 0)));

	}

	
}
