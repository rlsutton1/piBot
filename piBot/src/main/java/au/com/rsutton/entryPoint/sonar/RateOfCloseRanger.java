package au.com.rsutton.entryPoint.sonar;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;

public class RateOfCloseRanger implements ScanningSonarListener
{

	Map<Long, RangeRateData> data = new HashMap<Long, RangeRateData>();
	private RangeDataListener listener;

	public RateOfCloseRanger(RangeDataListener listener)
	{
		this.listener = listener;

	}

	RangeRateData translate(RangeData rangeData)
	{
		RangeRateData lastData = data.get(rangeData.getAngle());
		if (lastData == null)
		{
			// no previous data available, so...
			// current time and angle, speed is 0 and rateofclose is 0
			lastData = new RangeRateData(new Time(
					System.currentTimeMillis() - 1000, TimeUnit.MILLISECONDS),
					rangeData.getAngle(), rangeData.getDistance(), new Speed(
							new Distance(0, DistanceUnit.CM), new Time(
									System.currentTimeMillis() - 1000,
									TimeUnit.MILLISECONDS)), 0);
		}

		Time interval = new Time(rangeData.getTime().convert(TimeUnit.SECONDS)
				- lastData.getTime().convert(TimeUnit.SECONDS),
				TimeUnit.SECONDS);
		Distance changeInDistance = new Distance(lastData.getDistance()
				.convert(DistanceUnit.CM)
				- rangeData.getDistance().convert(DistanceUnit.CM),
				DistanceUnit.CM);
		Speed rateOfClose = new Speed(changeInDistance, interval);
		double changeInRateOfClose = rateOfClose.getSpeed(DistanceUnit.CM,
				TimeUnit.SECONDS)
				- lastData.getRateOfClose().getSpeed(DistanceUnit.CM,
						TimeUnit.SECONDS);

		RangeRateData newData = new RangeRateData(rangeData.getTime(),
				rangeData.getAngle(), rangeData.getDistance(), rateOfClose,
				changeInRateOfClose);
		data.put(rangeData.getAngle(), newData);

		return newData;

	}

	@Override
	public void notifiyDistance(RangeData data)
	{
		listener.notifyRangeData(translate(data));

	}

}
