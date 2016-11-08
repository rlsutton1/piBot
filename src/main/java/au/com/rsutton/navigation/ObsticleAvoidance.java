package au.com.rsutton.navigation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.mapping.particleFilter.ParticleFilterIfc;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.robot.RobotListener;
import au.com.rsutton.robot.rover.Angle;
import au.com.rsutton.robot.rover.LidarObservation;
import au.com.rsutton.robot.rover.MovingLidarObservationMultiBuffer;
import au.com.rsutton.ui.DataSourceMap;

public class ObsticleAvoidance
{
	MovingLidarObservationMultiBuffer scanBuffer;

	private Double awayFromObsticle;

	protected Double correctionAngle;

	protected Angle currentHeading;

	private Double correction;

	private Vector3D sp1;
	private Vector3D sp2;

	ObsticleAvoidance(RobotInterface robot)
	{
		scanBuffer = new MovingLidarObservationMultiBuffer(2);

		robot.addMessageListener(new RobotListener()
		{

			@Override
			public void observed(RobotLocation observation)
			{
				scanBuffer.addObservation(observation);

				currentHeading = observation.getDeadReaconingHeading();

				List<LidarObservation> observations = scanBuffer.getObservations(observation);

				double requiredObsticalClearance = 50;

				List<LidarObservation> closest = getClosest(observations, requiredObsticalClearance + 10);
				if (closest.size() == 2)
				{

					LidarObservation p1 = closest.get(0);
					LidarObservation p2 = closest.get(1);

					sp1 = p1.getVector();
					sp2 = p2.getVector();

					// ensure the points are in clockwise order ... might have
					// the direction wrong here

					double delta = HeadingHelper.getChangeInHeading(Math.toDegrees(p1.getAngleRadians()),
							Math.toDegrees(p2.getAngleRadians()));

					if (delta < 0)
					{
						LidarObservation t = p1;
						p1 = p2;
						p2 = t;
					}
					double x = p1.getX() - p2.getX();
					double y = p1.getY() - p2.getY();

					awayFromObsticle = Math.toDegrees(Math.atan2(y, x));

					double distanceToObsticle = p1.getDisctanceCm();
					if (distanceToObsticle < requiredObsticalClearance)
					{

						double distanceForCorrection = 80;

						correctionAngle = Math.toDegrees(
								Math.atan2(requiredObsticalClearance - distanceToObsticle, distanceForCorrection));

					} else
					{
						awayFromObsticle = null;
					}
				} else
				{
					awayFromObsticle = null;
				}
			}

			private List<LidarObservation> getClosest(List<LidarObservation> observations, double maxDistance)
			{

				List<LidarObservation> closest = new LinkedList<>();

				// prune list
				List<LidarObservation> prunedList = new LinkedList<>();

				LidarObservation last = null;
				for (LidarObservation obs : observations)
				{
					if (last != null)
					{
						if (last.getVector().distance(obs.getVector()) > 20)
						{
							prunedList.add(last);
							last = obs;
						}
					} else
					{
						last = obs;
					}

				}

				// find the furthest point that is within maxDistance
				double min = 0;
				for (LidarObservation observation : prunedList)
				{
					if (observation.getDisctanceCm() < maxDistance && observation.getDisctanceCm() > min)
					{
						closest.clear();
						closest.add(observation);
						min = observation.getDisctanceCm();
					}
				}

				// now we know the max find the 2 that are nearest
				double max = min;
				for (LidarObservation observation : prunedList)
				{
					if (observation.getDisctanceCm() < max)
					{

						closest.add(observation);
						max = observation.getDisctanceCm();
						if (closest.size() > 2)
						{
							closest.remove(0);
						}

					}
				}
				if (closest.size() < 2)
				{
					closest.clear();
				}

				return closest;
			}

		});
	}

	double getCorrectedHeading(double relativeHeading)
	{
		Double correctedRelativeHeading = relativeHeading;
		Double away = awayFromObsticle;
		Double ca = correctionAngle;
		correction = null;
		if (away != null)
		{
			// correctedHeading = currentHeading.getDegrees() - goalHeading;
			// there is an obsticle near by

			System.out.println(correctedRelativeHeading + " " + away);
			// compare goal direction to the direction to the obsticle
			double delta = HeadingHelper.getChangeInHeading(correctedRelativeHeading, away);
			if (Math.abs(delta) > 90)
			{
				// goal heading is towards the obsticle!
				if (delta < 0)
				{
					// avoid obsticle clock wise
					double clockWise = away - 90;
					correctedRelativeHeading = clockWise + Math.abs(ca);
				} else
				{
					// avoid obsitcle anti clock wise
					double antiClockWise = away + 90;
					correctedRelativeHeading = antiClockWise - Math.abs(ca);
				}
				if (Math.abs(HeadingHelper.getChangeInHeading(correctedRelativeHeading, away)) > 90)
				{
					System.out.println("Bad correction!");
				}
			}
			correction = correctedRelativeHeading;
		}

		return correctedRelativeHeading;
	}

	public DataSourceMap getHeadingMapDataSource(final ParticleFilterIfc pf, RobotInterface robot)
	{
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

				graphics.setColor(new Color(255, 255, 255));
				// draw lidar observation lines

				double pfh = pf.getAverageHeading();

				if (awayFromObsticle != null)
				{
					Vector3D vector = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(awayFromObsticle + pfh))
							.applyTo(new Vector3D(0, 40, 0));

					graphics.drawLine((int) pointOriginX, (int) pointOriginY,
							(int) (pointOriginX + (vector.getX() * scale)),
							(int) (pointOriginY + (vector.getY() * scale)));

					if (correction != null)
					{
						graphics.setColor(new Color(0, 255, 0));
						// correctionAngle
						vector = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(correction + pfh))
								.applyTo(new Vector3D(0, 40, 0));

						graphics.drawLine((int) pointOriginX, (int) pointOriginY,
								(int) (pointOriginX + (vector.getX() * scale)),
								(int) (pointOriginY + (vector.getY() * scale)));
					}

					graphics.setColor(new Color(0, 255, 0));
					// correctionAngle

					Vector3D p1 = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(pfh)).applyTo(sp1);
					Vector3D p2 = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(pfh)).applyTo(sp2);

					graphics.drawLine((int) ((pointOriginX + (p1.getX() * scale))),
							(int) ((pointOriginY + (p1.getY() * scale))), (int) ((pointOriginX + (p2.getX() * scale))),
							(int) ((pointOriginY + (p2.getY() * scale))));
				}
			}

		};
	}

}
