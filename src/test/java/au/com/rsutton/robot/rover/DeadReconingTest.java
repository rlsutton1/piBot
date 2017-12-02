package au.com.rsutton.robot.rover;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.AngleUnits;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;
import au.com.rsutton.units.Speed;

public class DeadReconingTest
{

	@Test
	public void testTurnRight()
	{
		DeadReconing dr = new DeadReconing(new Angle(0, AngleUnits.DEGREES), null);

		Distance leftDistance = new Distance(50, DistanceUnit.CM);
		Distance rightDistance = new Distance(25, DistanceUnit.CM);
		dr.updateLocation(getWheelController(leftDistance, rightDistance));

		assertTrue(dr.getHeading().getHeading() < 0 || dr.getHeading().getHeading() > 180);

	}

	@Test
	public void testTurnRightContinousForward()
	{
		DeadReconing dr = new DeadReconing(new Angle(0, AngleUnits.DEGREES), null);

		double lastHeading = 0;
		for (int i = 1; i < 30; i++)

		{
			Distance leftDistance = new Distance((i * 20), DistanceUnit.CM);
			Distance rightDistance = new Distance((i * 10), DistanceUnit.CM);
			dr.updateLocation(getWheelController(leftDistance, rightDistance));

			double heading = dr.getHeading().getHeading();

			double change = HeadingHelper.getChangeInHeading(heading, lastHeading);

			lastHeading = heading;

			assertTrue(change < -25 && change > -35);
		}
	}

	@Test
	public void testTurnLeftContinousForward()
	{
		DeadReconing dr = new DeadReconing(new Angle(0, AngleUnits.DEGREES), null);

		double lastHeading = 0;
		for (int i = 1; i < 30; i++)

		{
			Distance leftDistance = new Distance((i * 10), DistanceUnit.CM);
			Distance rightDistance = new Distance((i * 20), DistanceUnit.CM);
			dr.updateLocation(getWheelController(leftDistance, rightDistance));

			double heading = dr.getHeading().getHeading();

			double change = HeadingHelper.getChangeInHeading(heading, lastHeading);

			lastHeading = heading;

			assertTrue(change > 25 && change < 35);
		}
	}

	@Test
	public void testTurnLeftContinousSkid()
	{
		DeadReconing dr = new DeadReconing(new Angle(0, AngleUnits.DEGREES), null);

		double lastHeading = 0;
		for (int i = 1; i < 30; i++)

		{
			Distance leftDistance = new Distance((i * 0), DistanceUnit.CM);
			Distance rightDistance = new Distance((i * 20), DistanceUnit.CM);
			dr.updateLocation(getWheelController(leftDistance, rightDistance));

			double heading = dr.getHeading().getHeading();

			double change = HeadingHelper.getChangeInHeading(heading, lastHeading);

			lastHeading = heading;

			assertTrue(change > 55 && change < 60);
		}
	}

	@Test
	public void testTurnRightContinousSkid()
	{
		DeadReconing dr = new DeadReconing(new Angle(0, AngleUnits.DEGREES), null);

		double lastHeading = 0;
		for (int i = 1; i < 30; i++)

		{
			Distance leftDistance = new Distance((i * 20), DistanceUnit.CM);
			Distance rightDistance = new Distance((i * 0), DistanceUnit.CM);
			dr.updateLocation(getWheelController(leftDistance, rightDistance));

			double heading = dr.getHeading().getHeading();

			double change = HeadingHelper.getChangeInHeading(heading, lastHeading);

			lastHeading = heading;

			assertTrue(change < -55 && change > -60);
		}
	}

	WheelController getWheelController(final Distance leftDistance, final Distance rightDistance)
	{
		return new WheelController()
		{

			@Override
			public void setSpeed(Speed leftSpeed, Speed rightSpeed)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public Distance getDistanceRightWheel()
			{
				return rightDistance;
			}

			@Override
			public Distance getDistanceLeftWheel()
			{
				return leftDistance;
			}
		};
	}

	@Test
	public void testTurnRightContinousOnSpot()
	{
		DeadReconing dr = new DeadReconing(new Angle(0, AngleUnits.DEGREES), null);

		double lastHeading = 0;
		for (int i = 1; i < 30; i++)

		{
			Distance leftDistance = new Distance((i * 20), DistanceUnit.CM);
			Distance rightDistance = new Distance((i * -20), DistanceUnit.CM);
			dr.updateLocation(getWheelController(leftDistance, rightDistance));

			double heading = dr.getHeading().getHeading();

			double change = HeadingHelper.getChangeInHeading(heading, lastHeading);

			lastHeading = heading;

			assertTrue(change > -120 && change < -110);
		}
	}

	@Test
	public void testTurnLeftContinousOnSpot()
	{
		DeadReconing dr = new DeadReconing(new Angle(0, AngleUnits.DEGREES), null);

		double lastHeading = 0;
		for (int i = 1; i < 30; i++)

		{
			Distance leftDistance = new Distance((i * -20), DistanceUnit.CM);
			Distance rightDistance = new Distance((i * 20), DistanceUnit.CM);
			dr.updateLocation(getWheelController(leftDistance, rightDistance));

			double heading = dr.getHeading().getHeading();

			double change = HeadingHelper.getChangeInHeading(heading, lastHeading);

			lastHeading = heading;

			assertTrue(change < 120 && change > 110);
		}
	}

	@Test
	public void testTurnLeft()
	{
		DeadReconing dr = new DeadReconing(new Angle(0, AngleUnits.DEGREES), null);

		Distance leftDistance = new Distance(25, DistanceUnit.CM);
		Distance rightDistance = new Distance(50, DistanceUnit.CM);
		dr.updateLocation(getWheelController(leftDistance, rightDistance));

		assertTrue(dr.getHeading().getHeading() > 0 && dr.getHeading().getHeading() < 180);

	}

	@Test
	public void testTurnLeftNoRight()
	{
		DeadReconing dr = new DeadReconing(new Angle(0, AngleUnits.DEGREES), null);

		Distance leftDistance = new Distance(-25, DistanceUnit.CM);
		Distance rightDistance = new Distance(0, DistanceUnit.CM);
		dr.updateLocation(getWheelController(leftDistance, rightDistance));

		assertTrue(dr.getHeading().getHeading() > 0 && dr.getHeading().getHeading() < 180);

	}

}
