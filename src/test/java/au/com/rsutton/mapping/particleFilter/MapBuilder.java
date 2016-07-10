package au.com.rsutton.mapping.particleFilter;

import java.awt.Color;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.ui.MainPanel;

public class MapBuilder implements ParticleFilterListener
{

	private MainPanel panel;

	ProbabilityMap world = new ProbabilityMap(10);

	MapBuilder(ParticleFilter particleFilter)
	{
		panel = new MainPanel();

		panel.addDataSource(world, new Color(255, 255, 255));

		particleFilter.addListener(this);
	}

	int lastHeading = 0;

	@Override
	public void update(Vector3D averagePosition, double averageHeading, double stdDev,
			ParticleFilterObservationSet particleFilterObservationSet)
	{

		if (stdDev < 30 && Math.abs(lastHeading - averageHeading) < 3)
		{
			Vector3D pos = averagePosition;
			double x1 = pos.getX();
			double y1 = pos.getY();

			for (ScanObservation obs : particleFilterObservationSet.getObservations())
			{
				if (obs.getDisctanceCm() < 200)
				{

					Vector3D point = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(averageHeading)).applyTo(obs
							.getVector());
					point = pos.add(point);

					// clear out points

					// double x2 = point.getX();
					// double y2 = point.getY();
					//
					// double dist = obs.getDisctanceCm();
					// if (dist > 0)
					// {
					// for (int i = 0; i < dist; i++)
					// {
					// double percent = i / dist;
					// double x = (percent * x1) + ((1.0 - percent) * x2);
					// double y = (percent * y1) + ((1.0 - percent) * y2);
					// world.updatePoint((int) (x), (int) (y), -0.51, 2);
					// }
					// }

					world.updatePoint((int) (point.getX()), (int) (point.getY()), 0.51, 2);
				}
			}
		}
		lastHeading = (int) averageHeading;
	}
}
