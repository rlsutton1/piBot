package au.com.rsutton.mapping.multimap;

import java.util.LinkedList;
import java.util.List;

public class Averager
{
	private static final double ACCURACY_LIMIT = 80.0;

	public static final class Sample
	{

		public Sample(double x, double y, double heading, double accuracy)
		{
			this.x = x;
			this.y = y;
			this.heading = heading;
			this.accuracy = accuracy;

			// we assume the accuracy will always be better than 30
			this.quality = (ACCURACY_LIMIT - accuracy) / ACCURACY_LIMIT;
		}

		double x;
		double y;
		double heading;
		double accuracy;
		double quality;
	}

	List<Sample> samples = new LinkedList<>();

	public void addSample(double x, double y, double heading, double accuracy)
	{
		if (accuracy < ACCURACY_LIMIT)
		{
			System.out.println("adding sample");
			samples.add(new Sample(x, y, heading, accuracy));
		}
	}

	public Sample getRefinedValue()
	{
		double totalQuality = 0;
		for (Sample sample : samples)
		{
			// accuracy scaled 0 to 1
			totalQuality += sample.quality;
		}

		double x = 0;
		double y = 0;
		double heading = 0;
		double accuracy = 0;

		for (Sample sample : samples)
		{

			double scaler = sample.quality / totalQuality;
			x += sample.x * scaler;
			y += sample.y * scaler;
			heading += sample.heading * scaler;
			accuracy += sample.accuracy;

		}

		double averageAccuracy = accuracy / samples.size();

		// accuracy improves (divided by) by the square root of the number of
		// samples
		double improvedAccuracy = averageAccuracy / (Math.sqrt(samples.size()));
		return new Sample(x, y, heading, improvedAccuracy);
	}

}
