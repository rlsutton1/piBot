package au.com.rsutton.robot.rover;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class KalmanFilterForCompassTest
{

	@Test
	public void checkThatUsingValuesEitherSideOfZeroDontCauseProblems()
	{
		KalmanFilterForCompass filter = new KalmanFilterForCompass(new KalmanValue(0, 1));

		for (int i = 0; i < 100; i++)
		{
			filter.calculate(getDegree(1, 359));
			assertTrue((int)filter.getCurrentValue().getEstimate() == 359);
		}
		for (int i = 0; i < 100; i++)
		{
			filter.calculate(getDegree(359, 1));
			assertTrue((int)filter.getCurrentValue().getEstimate() == 0);
		}
	}

	private KalmanDataProvider getDegree(final int i, final int j)
	{
		return new KalmanDataProvider()
		{

			@Override
			public KalmanValue getObservation()
			{
				return new KalmanValue(j, 1);
			}

			@Override
			public KalmanValue getCalculatedNewValue(KalmanValue previousValue)
			{
				return new KalmanValue(i, 1);
			}
		};
	}

}
