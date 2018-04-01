package au.com.rsutton.navigation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.mapping.particleFilter.RobotPoseSource;
import au.com.rsutton.mapping.particleFilter.ScanObservation;
import au.com.rsutton.navigation.feature.RobotLocationDeltaListener;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.robot.lidar.LidarObservation;
import au.com.rsutton.robot.lidar.MovingLidarObservationMultiBuffer;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;

public class ObsticleAvoidance
{
	MovingLidarObservationMultiBuffer scanBuffer;

	private final AtomicReference<Double> awayFromObsticle = new AtomicReference<>();

	protected Double correctionAngle;

	private Double correction;

	private Vector3D sp1;
	private Vector3D sp2;

	final List<LidarObservation> currentObservations = new CopyOnWriteArrayList<>();

	double distanceForCorrection = 45;
	double requiredObsticalClearance = 35;

	ObsticleAvoidance(RobotInterface robot, RobotPoseSource pf)
	{
		scanBuffer = new MovingLidarObservationMultiBuffer(1, robot, pf);

		robot.addMessageListener(new RobotLocationDeltaListener()
		{

			@Override
			public void onMessage(Angle deltaHeading, Distance deltaDistance, List<ScanObservation> observation,
					boolean bump)
			{
				scanBuffer.addObservation(observation);

				currentObservations.clear();

				currentObservations.addAll(scanBuffer.getObservations());

				List<LidarObservation> closest = getClosest(currentObservations, requiredObsticalClearance + 10);
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

					awayFromObsticle.set(Math.toDegrees(Math.atan2(y, x)));

					double distanceToObsticle = p1.getDisctanceCm();
					if (distanceToObsticle < requiredObsticalClearance)
					{

						correctionAngle = Math.toDegrees(
								Math.atan2(requiredObsticalClearance - distanceToObsticle, distanceForCorrection));

					} else
					{
						awayFromObsticle.set(null);
					}
				} else
				{

					awayFromObsticle.set(null);
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
					double absObsAngle = Math
							.abs(HeadingHelper.getChangeInHeading(0, Math.toDegrees(obs.getAngleRadians())));
					if (absObsAngle < 90)
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

	CourseCorrection getCorrectedHeading(double relativeHeading, double desiredSpeed)
	{
		Double correctedRelativeHeading = relativeHeading;
		Double away = awayFromObsticle.get();
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
		double speed = desiredSpeed;
		if (sp1 != null)
		{
			if (sp1.getNorm() < requiredObsticalClearance * 0.9)
			{
				speed = desiredSpeed * 0.5;
			}
			if (correction != null && Math.abs(HeadingHelper.getChangeInHeading(relativeHeading, correction)) > 30)
			{
				speed = desiredSpeed * 0.25;
			}
		}
		return new CourseCorrection(correctedRelativeHeading, speed);

	}

	public DataSourceMap getHeadingMapDataSource(final RobotPoseSource pf, RobotInterface robot)
	{
		return new DataSourceMap()
		{

			@Override
			public List<Point> getPoints()
			{
				Vector3D pos = pf.getXyPosition().getVector(DistanceUnit.CM);
				List<Point> points = new LinkedList<>();
				points.add(new Point((int) pos.getX(), (int) pos.getY()));
				return points;
			}

			@Override
			public void drawPoint(BufferedImage image, double pointOriginX, double pointOriginY, double scale,
					double originalX, double originalY)
			{
				Graphics graphics = image.getGraphics();

				graphics.setColor(new Color(255, 255, 255));
				// draw lidar observation lines

				double pfh = pf.getHeading();
				Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(pfh));

				if (awayFromObsticle.get() != null)
				{
					Vector3D vector = new Rotation(RotationOrder.XYZ, 0, 0,
							Math.toRadians(awayFromObsticle.get() + pfh)).applyTo(new Vector3D(0, 40, 0));

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

					Vector3D p1 = rotation.applyTo(sp1);
					Vector3D p2 = rotation.applyTo(sp2);

					graphics.drawLine((int) ((pointOriginX + (p1.getX() * scale))),
							(int) ((pointOriginY + (p1.getY() * scale))), (int) ((pointOriginX + (p2.getX() * scale))),
							(int) ((pointOriginY + (p2.getY() * scale))));
				}

				// draw scan buffer points
				graphics.setColor(Color.GREEN);

				for (LidarObservation point : currentObservations)
				{
					Vector3D p1 = rotation.applyTo(point.getVector());
					int x = (int) ((pointOriginX + (p1.getX() * scale)));
					int y = (int) ((pointOriginY + (p1.getY() * scale)));
					graphics.drawLine(x, y, x + 3, y + 3);
				}
			}

		};
	}

}
