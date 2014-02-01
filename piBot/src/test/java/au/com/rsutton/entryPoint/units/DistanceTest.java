package au.com.rsutton.entryPoint.units;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DistanceTest
{

	@Test
	public void test()
	{
		Distance d1 = new Distance(25,DistanceUnit.CM);
		Distance d2 = new Distance(250,DistanceUnit.MM);
		Distance d3 = new Distance(.250,DistanceUnit.M);
		
		System.out.println(d1+" "+d2+" "+d3);
		assertTrue(d1.equals(d2));
		
		assertTrue(d1.equals(d3));
		assertTrue(d2.equals(d3));
		
		assertTrue(d1.convert(DistanceUnit.CM)== 25);
		assertTrue(d2.convert(DistanceUnit.CM)== 25);
		assertTrue(d3.convert(DistanceUnit.CM)== 25);
		
		assertTrue(d1.convert(DistanceUnit.MM)== 250);
		assertTrue(d2.convert(DistanceUnit.MM)== 250);
		assertTrue(d3.convert(DistanceUnit.MM)== 250);
		
		assertTrue(d1.convert(DistanceUnit.M)== .25);
		assertTrue(d2.convert(DistanceUnit.M)== .25);
		assertTrue(d3.convert(DistanceUnit.M)== .25);
	}

}
