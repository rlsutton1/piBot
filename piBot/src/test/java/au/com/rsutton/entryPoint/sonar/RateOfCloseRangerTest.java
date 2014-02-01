package au.com.rsutton.entryPoint.sonar;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;

public class RateOfCloseRangerTest
{
	int t;
	private long timeOffset;

	@Test
	public void test()
	{

		RateOfCloseRanger rocr = new RateOfCloseRanger(new RangeDataListener()
		{

			@Override
			public void notifyRangeData(RangeRateData rangeData)
			{
				assertTrue("bad angle", rangeData.getAngle() == 0);
				assertTrue(
						"bad distance",
						rangeData.getDistance().equals(
								new Distance(t, DistanceUnit.CM)));
				assertTrue(
						"bad time",
						rangeData.getTime().equals(
								new Time(timeOffset + (t * 1000),
										TimeUnit.MILLISECONDS)));
				if (t == 0)
				{
					// first sample, no data so rateofclose is 0, as is the
					// changeinrateofclose
					assertTrue(
							"bad rate of close t:" + t + " rate "
									+ rangeData.getRateOfClose(),
							rangeData.getRateOfClose().equals(
									new Speed(new Distance(0, DistanceUnit.CM),
											new Time(1, TimeUnit.SECONDS))));
					assertTrue(
							"bad change t:" + t + " change "
									+ rangeData.getChangeInRateOfClose(),
							rangeData.getChangeInRateOfClose() == 0);
				} else if (t == 1)
				{
					// rate of close is -1 and change is -1
					assertTrue(
							"bad rate of close t:" + t + " rate "
									+ rangeData.getRateOfClose(),
							rangeData.getRateOfClose().equals(
									new Speed(
											new Distance(-1, DistanceUnit.CM),
											new Time(1, TimeUnit.SECONDS))));
					assertTrue(
							"bad change t:" + t + " change "
									+ rangeData.getChangeInRateOfClose(),
							rangeData.getChangeInRateOfClose() == -1);

				} else if (t == 1)
				{
					assertTrue(
							"bad rate of close t:" + t + " rate "
									+ rangeData.getRateOfClose(),
							rangeData.getRateOfClose().equals(
									new Speed(
											new Distance(-1, DistanceUnit.CM),
											new Time(1, TimeUnit.SECONDS))));
					assertTrue(
							"bad change t:" + t + " change "
									+ rangeData.getChangeInRateOfClose(),
							rangeData.getChangeInRateOfClose() == 0);

				}

			}
		});

		timeOffset = System.currentTimeMillis();
		for (t = 0; t < 10; t++)
		{
			rocr.notifiyDistance(new RangeData(0, new Distance(t,
					DistanceUnit.CM), new Time(timeOffset + (t * 1000),
					TimeUnit.MILLISECONDS)));
		}
	}

}
