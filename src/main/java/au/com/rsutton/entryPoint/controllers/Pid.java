package au.com.rsutton.entryPoint.controllers;


/*
 * 
 * previous_error = setpoint - actual_position
 * integral = 0
 * start:
 * error = setpoint - actual_position
 * integral = integral + (error*dt)
 * derivative = (error - previous_error)/dt
 * output = (Kp*error) + (Ki*integral) + (Kd*derivative)
 * previous_error = error
 * wait(dt)
 * goto start
 * 
 */
public class Pid
{

	Double lastCheckTime = null;

	// used to persist the last error, until a current error occurs.
	Double lastError = null;
	private double kp;
	private double ki;
	private double kd;
	private double integral;
	private double div;
	private double maxOut;
	private double minOut;
	private boolean invert;

	/**
	 * 
	 * @param p
	 *            - error proportioning
	 * @param i
	 *            - over shoot scaler
	 * @param d
	 *            - previous error proportioning (Damping)
	 * @param div
	 *            - time interval expressed in milliseconds
	 */
	public Pid(double p, double i, double d, double div, double maxOut, double minOut, boolean invertOut)
	{
		kp = p;
		ki = i;
		kd = d;
		lastError = 0.0;
		integral = 0.0;
		this.div = div;
		this.maxOut = maxOut;
		this.minOut = minOut;
		this.invert = invertOut;

	}

	public double computePid(double target, double value)
	{
		double error;
		double interval;
		double now = System.currentTimeMillis();

		// calculate instantaneous error
		error = target - value;

		if (lastCheckTime == null)
		{
			// if first time through, assume last error = current error
			lastError = error;
			interval = 1;// seconds since last check
		} else
		{
			interval = (now - lastCheckTime) / div;// time since last check
		}
		lastCheckTime = now;
		
		integral += (error * interval);
		double derivative = (error - lastError) * interval;

		double i = ki * integral;
		double p = kp * error;
		double d = kd * derivative;

		double ret = p + i + d;

		if (invert)
		{
			ret = -ret;
		}

		if (ret > maxOut)
		{
			ret = maxOut;
			// we are at max output, so deduct away the integral we just added to avoid a build up
			integral -= (error * interval);
		}
		if (ret < minOut)
		{
			ret = minOut;
			// we are at min output, so deduct away the integral we just added to avoid a build up
			integral -= (error * interval);
		}

		lastError =  error;
		
//		System.out.printf("error %4.1f p: %4.1f i: %4.1f d: %4.1f output: %4.1f\n", error, p, i, d, ret);

		return ret;
	}

}