package au.com.rsutton.entryPoint.sonar;

import org.junit.Test;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;

public class SharpIRTest
{

	@Test
	public void  test()
	{
		SharpIR leftSonar = new SharpIR(2600000, 0,-1.25d);
		Distance d10 = leftSonar.getCurrentDistance(9000);// 10 cm
		Distance d20 = leftSonar.getCurrentDistance(8427);// 20 cm
		double delta1 = d10.convert(DistanceUnit.MM)/d20.convert(DistanceUnit.MM);
		System.out.println(leftSonar.getCurrentDistance(7384));// 30 cm
		Distance d50 = leftSonar.getCurrentDistance(6063);// 50 cm
		Distance d100 = leftSonar.getCurrentDistance(5436);// 100 cm
		double delta2 = d50.convert(DistanceUnit.MM)/d100.convert(DistanceUnit.MM);
		
		System.out.println(delta1);
		System.out.println(delta2);
		System.out.println(delta1/delta2);
		System.out.println(d10);
		System.out.println(d20);
//		System.out.println(d30);
		System.out.println(d50);
		System.out.println(d100);

		
	}
}
