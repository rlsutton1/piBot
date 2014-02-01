package au.com.rsutton.entryPoint;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import au.com.rsutton.entryPoint.sonar.RangeRateData;
import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;

public class ControllerSideTest
{

	@Test
	public void test55cmToSide() throws InterruptedException, IOException
	{
		final Controller con = new Controller();

		// 100cm, direct ahead, no rate of close expect forward speed 100%
		int angle = 90;
		int distanceCM = 55;
		int rateOfCloseCMs = 0;
		int changeInRateOfClose = 0;
		int headingChange = getResult(con, angle, distanceCM, rateOfCloseCMs,
				changeInRateOfClose);
		assertTrue("" + headingChange + ", expected 0", headingChange >-2 && headingChange < 2);

		
	}


	@Test
	public void test50cmToSide() throws InterruptedException, IOException
	{
		final Controller con = new Controller();

		// 100cm, direct ahead, no rate of close expect forward speed 100%
		int angle = 90;
		int distanceCM = 50;
		int rateOfCloseCMs = 0;
		int changeInRateOfClose = 0;
		int headingChange = getResult(con, angle, distanceCM, rateOfCloseCMs,
				changeInRateOfClose);
		assertTrue("" + headingChange + ", expected 0", headingChange >-2 && headingChange < 2);

		
	}

	@Test
	public void test25cmToSide() throws InterruptedException, IOException
	{
		final Controller con = new Controller();

		// 100cm, direct ahead, no rate of close expect forward speed 100%
		int angle = -90;
		int distanceCM = 25;
		int rateOfCloseCMs = 0;
		int changeInRateOfClose = 0;
		int headingChange = getResult(con, angle, distanceCM, rateOfCloseCMs,
				changeInRateOfClose);
		assertTrue("" + headingChange + ", expected 3", headingChange >1 && headingChange < 3);

		
	}

	@Test
	public void test5cmToSide() throws InterruptedException, IOException
	{
		final Controller con = new Controller();

		// 100cm, direct ahead, no rate of close expect forward speed 100%
		int angle = -90;
		int distanceCM = 5;
		int rateOfCloseCMs = 0;
		int changeInRateOfClose = 0;
		int headingChange = getResult(con, angle, distanceCM, rateOfCloseCMs,
				changeInRateOfClose);
		assertTrue("" + headingChange + ", expected 0", headingChange >3 && headingChange < 5);

		
	}
	
	@Test
	public void test10cmRateAtSide() throws InterruptedException, IOException
	{
		final Controller con = new Controller();

		// 100cm, direct ahead, no rate of close expect forward speed 100%
		int angle = 0;
		int distanceCM = 100;
		int rateOfCloseCMs = 10;
		int changeInRateOfClose = 0;
		int headingChange = getResult(con, angle, distanceCM, rateOfCloseCMs,
				changeInRateOfClose);
		assertTrue("" + headingChange + ", expected 0", headingChange >4 && headingChange < 7);

		
	}

	@Test
	public void test5cmRateAtSide() throws InterruptedException, IOException
	{
		final Controller con = new Controller();

		// 100cm, direct ahead, no rate of close expect forward speed 100%
		int angle = 90;
		int distanceCM = 100;
		int rateOfCloseCMs = 5;
		int changeInRateOfClose = 0;
		int headingChange = getResult(con, angle, distanceCM, rateOfCloseCMs,
				changeInRateOfClose);
		assertTrue("" + headingChange + ", expected 0", headingChange >1 && headingChange < 3);

		
	}

	@Test
	public void test0cmRateAtSide() throws InterruptedException, IOException
	{
		final Controller con = new Controller();

		// 100cm, direct ahead, no rate of close expect forward speed 100%
		int angle = -90;
		int distanceCM = 100;
		int rateOfCloseCMs = 0;
		int changeInRateOfClose = 0;
		int headingChange = getResult(con, angle, distanceCM, rateOfCloseCMs,
				changeInRateOfClose);
		assertTrue("" + headingChange + ", expected 0", headingChange ==0);

		
	}

	private int getResult(final Controller con, int angle, int distanceCM,
			int rateOfCloseCMs, int changeInRateOfClose)
	{
		RangeRateData data = createRangeRateData(angle, distanceCM,
				rateOfCloseCMs, changeInRateOfClose);
		int speed = con.processLatteralPressure(data);
		return speed;
	}

	private RangeRateData createRangeRateData(int angle, int distanceCM,
			int rateOfCloseCMs, int changeInRateOfClose)
	{
		Distance proximity = new Distance(distanceCM, DistanceUnit.CM);

		Distance changeInProximity = new Distance(rateOfCloseCMs,
				DistanceUnit.CM);
		Speed rateOfclose = new Speed(changeInProximity, new Time(1,
				TimeUnit.SECONDS));
		RangeRateData data = new RangeRateData(new Time(1, TimeUnit.SECONDS),
				angle, proximity, rateOfclose, changeInRateOfClose);
		return data;
	}
}
