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

public class ControllerForwardTest
{

	@Test
	public void test150cmAhead() throws InterruptedException, IOException
	{
		final Controller con = new Controller();

		// 100cm, direct ahead, no rate of close expect forward speed 100%
		int angle = 0;
		int distanceCM = 150;
		int rateOfCloseCMs = 0;
		int changeInRateOfClose = 0;
		int setSpeed = getResult(con, angle, distanceCM, rateOfCloseCMs,
				changeInRateOfClose);
		assertTrue("" + setSpeed + ", expected 100", setSpeed >98 && setSpeed < 101);

		
	}


	@Test
	public void test100cmAhead() throws InterruptedException, IOException
	{
		final Controller con = new Controller();

		// 100cm, direct ahead, no rate of close expect forward speed 100%
		int angle = 0;
		int distanceCM = 150;
		int rateOfCloseCMs = 0;
		int changeInRateOfClose = 0;
		int setSpeed = getResult(con, angle, distanceCM, rateOfCloseCMs,
				changeInRateOfClose);
		assertTrue("" + setSpeed + ", expected 100", setSpeed >98 && setSpeed < 101);

		
	}
	
	@Test
	public void test75cmAhead() throws InterruptedException, IOException
	{
		final Controller con = new Controller();

		// 100cm, direct ahead, no rate of close expect forward speed 100%
		int angle = 0;
		int distanceCM = 75;
		int rateOfCloseCMs = 0;
		int changeInRateOfClose = 0;
		int setSpeed = getResult(con, angle, distanceCM, rateOfCloseCMs,
				changeInRateOfClose);
		assertTrue("" + setSpeed + ", expected 75", setSpeed >64 && setSpeed < 67);

		
	}
	
	@Test
	public void test50cmAhead() throws InterruptedException, IOException
	{
		final Controller con = new Controller();

		// 100cm, direct ahead, no rate of close expect forward speed 100%
		int angle = 0;
		int distanceCM = 50;
		int rateOfCloseCMs = 0;
		int changeInRateOfClose = 0;
		int setSpeed = getResult(con, angle, distanceCM, rateOfCloseCMs,
				changeInRateOfClose);
		assertTrue("" + setSpeed + ", expected 45", setSpeed >32 && setSpeed < 35);

		
	}
	
	@Test
	public void test25cmAhead() throws InterruptedException, IOException
	{
		final Controller con = new Controller();

		// 100cm, direct ahead, no rate of close expect forward speed 100%
		int angle = 0;
		int distanceCM = 25;
		int rateOfCloseCMs = 0;
		int changeInRateOfClose = 0;
		int setSpeed = getResult(con, angle, distanceCM, rateOfCloseCMs,
				changeInRateOfClose);
		assertTrue("" + setSpeed + ", expected 0", setSpeed >-1 && setSpeed < 2);

		
	}
	
	@Test
	public void test10cmAhead() throws InterruptedException, IOException
	{
		final Controller con = new Controller();

		// 100cm, direct ahead, no rate of close expect forward speed 100%
		int angle = 0;
		int distanceCM = 10;
		int rateOfCloseCMs = 0;
		int changeInRateOfClose = 0;
		int setSpeed = getResult(con, angle, distanceCM, rateOfCloseCMs,
				changeInRateOfClose);
		assertTrue("" + setSpeed + ", expected 0", setSpeed >-1 && setSpeed < 2);

		
	}
	
	@Test
	public void test10cmRateOfClose() throws InterruptedException, IOException
	{
		final Controller con = new Controller();

		// 100cm, direct ahead, no rate of close expect forward speed 100%
		int angle = 0;
		int distanceCM = 100;
		int rateOfCloseCMs = 10;
		int changeInRateOfClose = 0;
		int setSpeed = getResult(con, angle, distanceCM, rateOfCloseCMs,
				changeInRateOfClose);
		assertTrue("" + setSpeed + ", expected 100", setSpeed >23 && setSpeed < 27);

		
	}
	
	@Test
	public void test5cmRateOfClose() throws InterruptedException, IOException
	{
		final Controller con = new Controller();

		// 100cm, direct ahead, no rate of close expect forward speed 100%
		int angle = 0;
		int distanceCM = 100;
		int rateOfCloseCMs = 5;
		int changeInRateOfClose = 0;
		int setSpeed = getResult(con, angle, distanceCM, rateOfCloseCMs,
				changeInRateOfClose);
		assertTrue("" + setSpeed + ", expected 75", setSpeed >73 && setSpeed < 77);

		
	}
	
	@Test
	public void test2cmRateOfClose() throws InterruptedException, IOException
	{
		final Controller con = new Controller();

		// 100cm, direct ahead, no rate of close expect forward speed 100%
		int angle = 0;
		int distanceCM = 100;
		int rateOfCloseCMs = 2;
		int changeInRateOfClose = 0;
		int setSpeed = getResult(con, angle, distanceCM, rateOfCloseCMs,
				changeInRateOfClose);
		assertTrue("" + setSpeed + ", expected 75", setSpeed >98 && setSpeed < 101);

		
	}


	private int getResult(final Controller con, int angle, int distanceCM,
			int rateOfCloseCMs, int changeInRateOfClose)
	{
		RangeRateData data = createRangeRateData(angle, distanceCM,
				rateOfCloseCMs, changeInRateOfClose);
		int speed = con.processForwardPressure(data);
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
