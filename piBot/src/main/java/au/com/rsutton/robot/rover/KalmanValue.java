package au.com.rsutton.robot.rover;

public class KalmanValue
{

	private double estimate;
	private double error;

	public KalmanValue(double newEstimate, double newError)
	{
		estimate = newEstimate;
		if (!(newError > 0))
		{
			throw new RuntimeException("error must be greater than zero, "
					+ "as a zero value breaks the Kalman Filter " + "and besides nothing is ever error free! :)");
		}

		error = newError;
	}

	public double getError()
	{
		return error;
	}

	public double getEstimate()
	{
		return estimate;
	}

}
