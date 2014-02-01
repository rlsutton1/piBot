package au.com.rsutton.entryPoint.controllers;

import java.util.Date;

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
	Double lastCurrentError = null;
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
		double now = new Date().getTime();
		if (lastCheckTime == null)
		{
			interval = 1;// seconds since last check
		} else
		{
			interval = (now - lastCheckTime) / div;// time since last check
		}
		lastCheckTime = now;

		// calculate instantaneous error
		error = target - value;

		// if first time through, assume last error = current error
		if (lastCurrentError == null || lastError == null)
		{
			lastCurrentError = error;
			lastError = error;
		}
		if (Math.abs(error - lastCurrentError) < 0.001)
		{
			// take the current error, and store it to last error for the next
			// call
			lastError = lastCurrentError;
			lastCurrentError = error;
		}

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
			integral *= .9;
		}
		if (ret < minOut)
		{
			ret = minOut;
			integral *= .9;
		}

//		System.out.printf("error %4.1f p: %4.1f i: %4.1f d: %4.1f output: %4.1f\n", error, p, i, d, ret);

		return ret;
	}

}