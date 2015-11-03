package au.com.rsutton.mapping.v3.impl;

import au.com.rsutton.mapping.XY;
import au.com.rsutton.robot.rover.Angle;

public class ObservationOrigin
{

	XY origin;
	Angle orientation;

	/**
	 * think orientation +- anlgeSpread ie 243+-5
	 */
	Angle angleSpread;

	public ObservationOrigin(XY xy, Angle angle, Angle spread)
	{
		origin = xy;
		orientation = angle;
		angleSpread = spread;
	}

	public Angle getOrientation()
	{
		return orientation;
	}

	public XY getLocation()
	{
		return origin;
	}
}
