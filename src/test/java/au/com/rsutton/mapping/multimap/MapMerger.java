package au.com.rsutton.mapping.multimap;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.mapping.multimap.Averager.Sample;
import au.com.rsutton.mapping.probability.Occupancy;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;

public class MapMerger
{

	static void mergeMaps(ProbabilityMapIIFc mainMap, ProbabilityMapIIFc addition, Sample offset)
	{
		Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(offset.heading));
		Vector3D offsetVector = new Vector3D(offset.x, offset.y, 0);

		int minX = addition.getMinX();
		int maxX = addition.getMaxX();

		int minY = addition.getMinY();
		int maxY = addition.getMaxY();

		// iterate map
		for (int x = minX; x <= maxX; x += addition.getBlockSize())
		{
			for (int y = minY; y <= maxY; y += addition.getBlockSize())
			{
				double mapValue = addition.get(x, y);
				if (Math.abs(0.5 - mapValue) > 0.05)
				{
					// apply the offset
					Vector3D source = new Vector3D(x, y, 0);
					Vector3D dest = rotation.applyTo(source.add(offsetVector));

					// update the target map
					double mainMapValue = mainMap.get((int) dest.getX(), (int) dest.getY());
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
