package au.com.rsutton.mapping.multimap;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.mapping.multimap.Averager.Sample;
import au.com.rsutton.mapping.particleFilter.Pose;
import au.com.rsutton.mapping.probability.Occupancy;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;

public class MapMerger
{

	static void mergeMaps(ProbabilityMapIIFc mainMap, ProbabilityMapIIFc addition, Sample offset,
			Pose robotsOriginalPoseInMap)
	{
		Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(offset.heading));
		Vector3D offsetVector = new Vector3D(offset.x, offset.y, 0);

		int minX = addition.getMinX();
		int maxX = addition.getMaxX();

		int minY = addition.getMinY();
		int maxY = addition.getMaxY();

		Vector3D originOfObservations = new Vector3D(robotsOriginalPoseInMap.getY(), robotsOriginalPoseInMap.getX(), 0);

		// iterate map
		for (int x = minX; x <= maxX; x += addition.getBlockSize())
		{
			for (int y = minY; y <= maxY; y += addition.getBlockSize())
			{
				// apply the offset
				Vector3D source = new Vector3D(x, y, 0);
				Vector3D dest = rotation.applyTo(source.add(offsetVector));
				double mapValue = addition.get(x, y);

				// update the target map
				double mainMapValue = mainMap.get((int) dest.getX(), (int) dest.getY());

				if (mainMapValue > 0.001 && mainMapValue < 0.999)
				{

					if (Vector3D.distance(originOfObservations, dest) < 150)
					{

						if (mapValue > 0.5)
						{
							mainMap.updatePoint((int) dest.getX(), (int) dest.getY(), Occupancy.OCCUPIED, 1, 1);
							if (mainMap.get(dest.getX(), dest.getY()) < 1.0)
							{
								throw new RuntimeException("wrong!");
							}
						}
						if (mapValue < 0.5)
						{
							mainMap.updatePoint((int) dest.getX(), (int) dest.getY(), Occupancy.VACANT, 1, 1);
							if (mainMap.get(dest.getX(), dest.getY()) > 0.0)
							{
								throw new RuntimeException("wrong!");
							}
						}

					} else
					{

						if (Math.abs(0.5 - mapValue) > 0.05)
						{

							if (Math.abs(mainMapValue - 0.5) < 0.01
									|| (Math.signum(0.5 - mainMapValue) == Math.signum(0.5 - mapValue)))
							{
								if (mapValue > 0.5)
								{
									mainMap.updatePoint((int) dest.getX(), (int) dest.getY(), Occupancy.OCCUPIED,
											(1.0 - Math.abs(mainMapValue - mapValue)), 1);
								} else
								{
									mainMap.updatePoint((int) dest.getX(), (int) dest.getY(), Occupancy.VACANT,
											(1.0 - Math.abs(mainMapValue - mapValue)), 1);

								}
							}
						}
					}
				}
			}
		}

	}
}
