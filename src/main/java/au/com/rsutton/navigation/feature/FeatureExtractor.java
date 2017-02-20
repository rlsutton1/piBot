package au.com.rsutton.navigation.feature;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.mapping.particleFilter.ParticleFilterIfc;
import au.com.rsutton.mapping.particleFilter.ScanObservation;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.robot.RobotListener;
import au.com.rsutton.ui.DataSourceMap;

public abstract class FeatureExtractor
{

	private void setupListener(RobotInterface robot)
	{
		robot.addMessageListener(new RobotListener()
		{

			@Override
			public void observed(RobotLocation robotLocation)
			{
				evaluateScan(robotLocation.getObservations());

			}

		});
	}

	public DataSourceMap getHeadingMapDataSource(final ParticleFilterIfc pf, RobotInterface robot)
	{
		setupListener(robot);
		return new DataSourceMap()
		{

			@Override
			public List<Point> getPoints()
			{
				Vector3D pos = pf.dumpAveragePosition();
				List<Point> points = new LinkedList<>();
				points.add(new Point((int) pos.getX(), (int) pos.getY()));
				return points;
			}

			@Override
			public void drawPoint(BufferedImage image, double pointOriginX, double pointOriginY, double scale)
			{
				Graphics graphics = image.getGraphics();

				// draw lidar observation lines
				for (Spike spike : currentSpikes)
				{
					graphics.setColor(new Color(255, 0, 0));
					Vector3D obs = new Vector3D(spike.x, spike.y, 0);

					Vector3D vector = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(pf.getAverageHeading()))
							.applyTo(obs);

					int pointX = (int) (pointOriginX + (vector.getX() * scale));
					int pointY = (int) (pointOriginY + (vector.getY() * scale));
					graphics.drawRect(pointX, pointY, 5, 5);

					double heading = pf.getAverageHeading();
					double direction = heading + spike.angle + 90;

					Vector3D line1 = new Vector3D(0, 30, 0);
					line1 = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(direction)).applyTo(line1);

					graphics.drawLine(pointX, pointY, (int) (pointX + line1.getX()), (int) (pointY + line1.getY()));

					direction = heading + spike.angleAwayFromWall + 90;

					Vector3D line = new Vector3D(0, 15, 0);
					line = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(direction)).applyTo(line);

					graphics.setColor(new Color(255, 255, 0));
					graphics.drawLine((int) (pointX + line1.getX()), (int) (pointY + line1.getY()),
							(int) (pointX + line1.getX() + line.getX()), (int) (pointY + line1.getY() + line.getY()));

				}

				currentSpikes.clear();

			}

		};
	}

	private List<ScanObservation> lastObs = new LinkedList<>();
	protected List<Spike> currentSpikes = new LinkedList<>();

	private void evaluateScan(List<ScanObservation> observations)
	{
		for (ScanObservation obs : observations)
		{
			lastObs.add(obs);
			List<Spike> spike = detectSpike(lastObs);

			currentSpikes.addAll(spike);

		}

	}

	/**
	 * return a list of ScanObservations when observations closer than 10cm
	 * together removed
	 * 
	 * @param lastObs2
	 * @param requiredSize
	 * @return
	 */
	protected List<ScanObservation> resampleData(List<ScanObservation> lastObs2, int requiredSize)
	{
		List<ScanObservation> ret = new LinkedList<>();
		if (!lastObs2.isEmpty())
		{
			ScanObservation last = lastObs2.get(0);
			ret.add(last);
			for (ScanObservation obs : lastObs2)
			{
				if (Vector3D.distance(last.getVector(), obs.getVector()) >= 10)
				{
					last = obs;
					ret.add(obs);
				}
				if (ret.size() == requiredSize)
				{
					lastObs2.remove(0);
					break;
				}
			}
		}
		return ret;

	}

	abstract List<Spike> detectSpike(List<ScanObservation> lastObs2);
}
