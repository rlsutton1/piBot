package au.com.rsutton.navigation.feature;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.mapping.particleFilter.ScanObservation;
import au.com.rsutton.robot.RobotInterface;

public class FeatureExtractorSpike extends FeatureExtractor
{

	public FeatureExtractorSpike(SpikeListener listener, RobotInterface robot)
	{
		super(listener, robot);
	}

	@Override
	List<Spike> detectSpike(List<ScanObservation> lastObs2)
	{
		List<Spike> ret = new LinkedList<>();
		if (lastObs2.size() > 5)
		{
			double d1 = lastObs2.get(0).getDisctanceCm();
			double d2 = lastObs2.get(1).getDisctanceCm();
			double d3 = lastObs2.get(2).getDisctanceCm();
			double d4 = lastObs2.get(3).getDisctanceCm();
			double d5 = lastObs2.get(4).getDisctanceCm();
			double d6 = lastObs2.get(5).getDisctanceCm();

			boolean hasSpike = Math.abs(d4 - d3) > 50;
			boolean hasPlane1 = Math.abs(Math.abs(d3 - d2) - Math.abs(d2 - d1)) < 20;
			boolean hasPlane2 = Math.abs(Math.abs(d6 - d5) - Math.abs(d5 - d4)) < 20;
			if (hasSpike && hasPlane1 && hasPlane2)
			{
				double deltaX;
				double deltaY;
				double x;
				double y;
				double planeLength;
				Vector3D plane1End = lastObs2.get(0).getVector();
				Vector3D plane1Center = lastObs2.get(2).getVector();
				Vector3D plane2Center = lastObs2.get(3).getVector();
				Vector3D plane2End = lastObs2.get(5).getVector();

				double angleAwayFromWall;
				if (d3 < d4)
				{
					// use plane 1

					deltaY = plane1Center.getY() - plane1End.getY();
					deltaX = plane1Center.getX() - plane1End.getX();
					x = plane1Center.getX();
					y = plane1Center.getY();
					angleAwayFromWall = Math.toDegrees(Math.atan2(deltaY, deltaX)) - 90;
				} else
				{
					// use plane 2

					deltaY = plane2Center.getY() - plane2End.getY();
					deltaX = plane2Center.getX() - plane2End.getX();
					x = plane2Center.getX();
					y = plane2Center.getY();
					angleAwayFromWall = Math.toDegrees(Math.atan2(deltaY, deltaX)) + 90;

				}

				planeLength = Vector3D.distance(plane2Center, plane2End);
				planeLength += Vector3D.distance(plane1Center, plane1End);

				// ensure the 3 points forming the plane aren't spread over a
				// great distance, as at distance we can detect false spikes
				if (planeLength < 100)
				{
					double angle = Math.toDegrees(Math.atan2(deltaY, deltaX));

					ret.add(new Spike(x, y, angle, angleAwayFromWall));
				}
			}
			lastObs2.remove(0);
		}
		return ret;

	}
}
