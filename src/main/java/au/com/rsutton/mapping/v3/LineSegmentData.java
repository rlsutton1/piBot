package au.com.rsutton.mapping.v3;

import au.com.rsutton.mapping.v3.impl.ObservedPoint;
import au.com.rsutton.mapping.v3.linearEquasion.LinearEquasion;
import au.com.rsutton.mapping.v3.linearEquasion.LinearEquasionFactory;

public class LineSegmentData
{

	private LinearEquasion lineAngle;
	private ObservedPoint secondaryPoint;
	private ObservedPoint primaryPoint;

	public LineSegmentData( double lineDistance,
			ObservedPoint point, ObservedPoint secondaryPoint)
	{
		this.lineAngle = LinearEquasionFactory.getEquasion(point, secondaryPoint);
		this.secondaryPoint = secondaryPoint;
		this.primaryPoint = point;
	}

	public LinearEquasion getAngle()
	{
		return lineAngle;
	}

	public ObservedPoint getSecondaryPoint()
	{
		return secondaryPoint;
	}

	public ObservedPoint getPrimaryPoint()
	{
		return primaryPoint;
	}

}
