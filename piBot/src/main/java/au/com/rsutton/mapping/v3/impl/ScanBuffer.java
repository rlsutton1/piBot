package au.com.rsutton.mapping.v3.impl;

import java.util.LinkedHashMap;
import java.util.Map;

public class ScanBuffer
{

	Map<Integer, ObservedPoint> scanAngleToPoint = new LinkedHashMap<>();

	ScanBufferListener listener;

	ObservationOrigin origin;

	ScanBuffer(ScanBufferListener listener)
	{
		this.listener = listener;
	}

	public void addObservation(ObservedPoint point)
	{

		if (origin != null)
		{
			if (haveWeSeenThisAngleAlready(point) || hasTheOriginChanged(point))
			{
				listener.processScanData(scanAngleToPoint.values());
				scanAngleToPoint.clear();
				origin = point.getObservedFrom();
			}
		} else
		{
			origin = point.getObservedFrom();
		}

		scanAngleToPoint.put((int) point.getObservedFrom().getOrientation()
				.getDegrees(), point);

	}

	private boolean haveWeSeenThisAngleAlready(ObservedPoint point)
	{
		return scanAngleToPoint.containsKey((int) point.getObservedFrom()
				.getOrientation().getDegrees());
	}

	private boolean hasTheOriginChanged(ObservedPoint point)
	{
		return Math.abs(origin.getOrientation().getDegrees()
				- point.getObservedFrom().getOrientation().getDegrees()) > 0.5
				|| !origin.getLocation().equals(
						point.getObservedFrom().getLocation());
	}

}
