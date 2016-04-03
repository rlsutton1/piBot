package au.com.rsutton.robot.rover;

import java.util.LinkedList;
import java.util.List;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;

public class KalmanFilterForCompass extends KalmanFilter
{

	/**
	 * Performs the following to facilitate handing compass angles with the
	 * KalmanFilter
	 * 
	 * Normalises of the compass headings such that the angle will be rotated by
	 * + or - 360 degrees if required, to make the values in the same range.
	 * 
	 * for example 350 and 10 degrees are only 20 degrees apart but numerically
	 * are 340 apart. by adding 360 to one of the values it becomes 350 and 370
	 * which are numerically 20 apart
	 * 
	 * after the filter is applied the heading is normalised to between 0 and
	 * 360
	 * 
	 * @param initialValue
	 */
	KalmanFilterForCompass(KalmanValue initialValue)
	{
		super(initialValue);
	}

	void calculate(final KalmanDataProvider dataProvider)
	{

		super.calculate(getAngleDataProvider(dataProvider));
		normaliseCalculatedValue();
	}

	void calculate(final KalmanMultiDataProvider dataProvider)
	{
		super.calculate(getAngleDataProvider(dataProvider));
		normaliseCalculatedValue();
	}

	void calculateNoPrediction(KalmanDataProvider dataProvider)
	{
		super.calculateNoPrediction(getAngleDataProvider(dataProvider));
		normaliseCalculatedValue();
	}

	private void normaliseCalculatedValue()
	{
		double normalized = HeadingHelper.normalizeHeading(value.get().getEstimate());
		value.set(new KalmanValue(normalized, value.get().getError()));
		System.out.println("Normalized heading " + normalized);
	}

	private KalmanDataProvider getAngleDataProvider(final KalmanDataProvider dataProvider)
	{
		return new KalmanDataProvider()
		{

			@Override
			public KalmanValue getObservation()
			{
				KalmanValue kalmanValue = dataProvider.getObservation();

				return calculateClosestAngle(kalmanValue);
			}

			@Override
			public KalmanValue getCalculatedNewValue(KalmanValue previousValue)
			{
				KalmanValue kalmanValue = dataProvider.getCalculatedNewValue(previousValue);
				return calculateClosestAngle(kalmanValue);
			}

		};
	}

	private KalmanMultiDataProvider getAngleDataProvider(final KalmanMultiDataProvider dataProvider)
	{
		return new KalmanMultiDataProvider()
		{

			@Override
			public List<KalmanValue> getObservations(final KalmanValue previousObservation)
			{
				List<KalmanValue> values = new LinkedList<>();
				for (KalmanValue value : dataProvider.getObservations(previousObservation))
				{
					values.add(calculateClosestAngle(value));
				}
				return values;
			}

		};
	}

	/**
	 * adjusts the angle to the nearest numerical value to that of the
	 * previousEstimate by adding or subtracting 360 degrees
	 * 
	 * @param dataProvider
	 * @return
	 */
	private KalmanValue calculateClosestAngle(KalmanValue kalmanValue)
	{
		double temp = kalmanValue.getEstimate();

		double diff = value.get().getEstimate() - kalmanValue.getEstimate();
		if (Math.abs(diff) > 180)
		{
			temp += Math.signum(diff) * 360.0;
		}

		return new KalmanValue(temp, kalmanValue.getError());
	}
}
