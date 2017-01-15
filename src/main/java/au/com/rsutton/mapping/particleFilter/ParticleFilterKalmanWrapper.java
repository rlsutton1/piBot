package au.com.rsutton.mapping.particleFilter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.robot.rover.KalmanFilterForCompass;
import au.com.rsutton.robot.rover.KalmanHelper;
import au.com.rsutton.robot.rover.KalmanValue;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.ui.DataSourcePoint;

public class ParticleFilterKalmanWrapper
{
	private final ParticleFilterImpl particleFilter;
	KalmanFilterForCompass headingFilter = new KalmanFilterForCompass(new KalmanValue(0, 360));

	// Vector3D lastKnownPosition;
	private AtomicReference<ParticleFilterObservationSet> lastObservation = new AtomicReference<>();

	KalmanHelper headingKalmanHelper = new KalmanHelper();

	ParticleFilterKalmanWrapper(ProbabilityMapIIFc map, int particles, double distanceNoise, double headingNoise)
	{
		particleFilter = new ParticleFilterImpl(map, particles, distanceNoise, headingNoise, StartPosition.RANDOM);

		// lastKnownPosition = particleFilter.dumpAveragePosition();

	}

	public void moveParticles(ParticleUpdate update)
	{
		particleFilter.moveParticles(update);
	}

	public void addObservation(ProbabilityMapIIFc currentWorld, ParticleFilterObservationSet observations,
			double compassAdjustment)
	{
		particleFilter.addObservation(currentWorld, observations, compassAdjustment);
		lastObservation.set(observations);
	}

	// public void resample(ProbabilityMap map)
	// {
	// particleFilter.resample(map);
	//
	// headingFilter.calculate(new KalmanDataProvider()
	// {
	//
	// @Override
	// public KalmanValue getObservation()
	// {
	// return new KalmanValue(particleFilter.getAverageHeading(), 50);
	// }
	//
	// @Override
	// public KalmanValue getCalculatedNewValue(KalmanValue previousValue)
	// {
	//
	// return new
	// KalmanValue(headingKalmanHelper.getValueBasedOnChangedDeadReconningValue(lastObservation
	// .get().getDeadReaconingHeading().getDegrees()), 50);
	// }
	// });
	//
	// headingKalmanHelper.setCurrentKalmanAndDeadReconningValue(headingFilter.getCurrentValue().getEstimate(),
	// lastObservation.get().getDeadReaconingHeading().getDegrees());
	//
	// // lastKnownPosition = particleFilter.dumpAveragePosition();
	//
	// }

	public void dumpTextWorld(ProbabilityMapIIFc map)
	{
		// particleFilter.dumpTextWorld(map);
	}

	public Vector3D dumpAveragePosition()
	{
		return particleFilter.dumpAveragePosition();
	}

	public double getAverageHeading()
	{
		return headingKalmanHelper
				.getValueBasedOnChangedDeadReconningValue(lastObservation.get().getDeadReaconingHeading().getDegrees());
	}

	public double getStdDev()
	{
		return particleFilter.getStdDev();
	}

	public Double getBestRating()
	{
		return particleFilter.getBestScanMatchScore();
	}

	public void setParticleCount(int i)
	{
		particleFilter.setParticleCount(i);
	}

	public DataSourcePoint getParticlePointSource()
	{
		return particleFilter.getParticlePointSource();
	}

	public DataSourceMap getHeadingMapDataSource()
	{
		// return particleFilter.getHeadingMapDataSource();

		final DataSourceMap pfmps = particleFilter.getHeadingMapDataSource();

		return new DataSourceMap()
		{

			@Override
			public List<Point> getPoints()
			{
				return pfmps.getPoints();
			}

			@Override
			public void drawPoint(BufferedImage image, double pointOriginX, double pointOriginY, double scale)
			{
				pfmps.drawPoint(image, pointOriginX, pointOriginY, scale);
				Graphics graphics = image.getGraphics();

				// draw robot body
				graphics.setColor(new Color(0, 128, 128));
				int robotSize = 30;
				graphics.drawOval((int) (pointOriginX - (robotSize * 0.5 * scale)),
						(int) (pointOriginY - (robotSize * 0.5 * scale)), (int) (robotSize * scale),
						(int) (robotSize * scale));

				double heading = getAverageHeading();

				if (lastObservation.get() != null)
				{
					graphics.setColor(new Color(0, 255, 0));
					// draw lidar observation lines
					for (ScanObservation obs : lastObservation.get().getObservations())
					{
						Vector3D vector = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(heading))
								.applyTo(obs.getVector());
						graphics.drawLine((int) pointOriginX, (int) pointOriginY,
								(int) (pointOriginX + (vector.getX() * scale)),
								(int) (pointOriginY + (vector.getY() * scale)));
					}
				}

				// draw heading line
				graphics.setColor(new Color(255, 0, 0));
				Vector3D line = new Vector3D(60 * scale, 0, 0);
				line = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(heading + 90)).applyTo(line);

				graphics.drawLine((int) pointOriginX, (int) pointOriginY, (int) (pointOriginX + line.getX()),
						(int) (pointOriginY + line.getY()));

			}
		};

	};
}
