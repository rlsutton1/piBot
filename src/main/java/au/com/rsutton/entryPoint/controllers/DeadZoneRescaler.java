package au.com.rsutton.entryPoint.controllers;

/**
 * rescale a pwm value so as to avoid the zone where the motor/gear box internal
 * resistance is greater than the power input
 * 
 * @author rsutton
 *
 */
public class DeadZoneRescaler
{

	private double range;
	private double deadzone;

	public DeadZoneRescaler(int range, int deadzone)
	{
		this.range = range;
		this.deadzone = deadzone;
	}

	int rescale(int value)
	{
		if (Math.abs(value) < 1)
		{
			return 0;
		}
		// remove sign
		double absValue = Math.abs(value);

		// calculate fraction of power
		double fraction = absValue / range;

		// rescale into non deadzone
		double output = (fraction * (range - deadzone)) + deadzone;

		// restore sign
		output = output * Math.signum(value);

		return (int) output;
	}
}
