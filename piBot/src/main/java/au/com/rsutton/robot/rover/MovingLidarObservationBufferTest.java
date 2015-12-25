package au.com.rsutton.robot.rover;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.hazelcast.RobotLocation;

public class MovingLidarObservationBufferTest
{

	@Test
	public void testAddLidarObservation()
	{
		MovingLidarObservationBuffer buffer = new MovingLidarObservationBuffer();

		RobotLocation robotLocation = new RobotLocation();
		robotLocation.setHeading(new Angle(0, AngleUnits.DEGREES));
		robotLocation.setX(new Distance(0, DistanceUnit.CM));
		robotLocation.setY(new Distance(0, DistanceUnit.CM));

		List<LidarObservation> observations = new LinkedList<LidarObservation>();
		observations.add(new LidarObservation(Vector3D.ZERO,false));

		robotLocation.setObservations(observations);

		buffer.addLidarObservation(robotLocation);
	}

	@Test
	public void testGetTranslatedObservationsX()
	{
		MovingLidarObservationBuffer buffer = new MovingLidarObservationBuffer();
		addObservation(buffer, 0, 20, 0, Vector3D.ZERO);
		// addObservation(buffer, 0, 0, 0, Vector3D.ZERO);

		List<LidarObservation> observations = buffer.getTranslatedObservations(
				new Rotation(RotationOrder.XYZ, 0, 0, 0), new Vector3D(10, 0, 0));
		assertTrue("Expected 10, got " + observations.get(0).getX(), observations.get(0).getX() == 10);
		assertTrue(observations.get(0).getY() == 0);
	}

	@Test
	public void testGetTranslatedObservationsY()
	{
		// at position 20, observe point with relative distance of zero
		MovingLidarObservationBuffer buffer = new MovingLidarObservationBuffer();
		addObservation(buffer, 0, 0, 20, Vector3D.ZERO);
		// addObservation(buffer, 0, 0, 0, Vector3D.ZERO);

		// move to position 10, point shoule now be observed at a relative
		// distance of 10 (20-10=10)
		List<LidarObservation> observations = buffer.getTranslatedObservations(
				new Rotation(RotationOrder.XYZ, 0, 0, 0), new Vector3D(0, 10, 0));
		assertTrue("Expected 10, got " + observations.get(0).getY(), observations.get(0).getY() == 10);
		assertTrue("Expected 0, got " + observations.get(0).getX(), observations.get(0).getX() == 0);
	}

	@Test
	public void testRotation()
	{
		// at position 10 on the x axis, observe point with relative distance of zero
		MovingLidarObservationBuffer buffer = new MovingLidarObservationBuffer();
		addObservation(buffer, 0, 10, 0, Vector3D.ZERO);
		// addObservation(buffer, 0, 0, 0, Vector3D.ZERO);

		// move to position 0, and rotate 90 degrees point should now be observed at a relative
		// distance of -10 in the y axis 
		List<LidarObservation> observations = buffer.getTranslatedObservations(
				new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(90)), new Vector3D(0, 0, 0));
		assertTrue("Expected -10, got " + observations.get(0).getY(), observations.get(0).getY() == -10);
		assertTrue("Expected 0, got " + observations.get(0).getX(), observations.get(0).getX() == 0);

	}
	
	@Test
	public void testMultipleObservationsAtDifferentLocationsWithTranslation()
	{
		// at position 20, observe point with relative distance of zero
		MovingLidarObservationBuffer buffer = new MovingLidarObservationBuffer();
		addObservation(buffer, 0, 0, 20, Vector3D.ZERO);
		addObservation(buffer, 0, 0, 11, Vector3D.ZERO);

		// move to position 10, point shoule now be observed at a relative
		// distance of 10 (20-10=10)
		List<LidarObservation> observations = buffer.getTranslatedObservations(
				new Rotation(RotationOrder.XYZ, 0, 0, 0), new Vector3D(0, 10, 0));
		assertTrue("Expected 10, got " + observations.get(0).getY(), observations.get(0).getY() == 10);
		assertTrue("Expected 0, got " + observations.get(0).getX(), observations.get(0).getX() == 0);

		assertTrue("Expected 1, got " + observations.get(1).getY(), observations.get(1).getY() == 1);
		assertTrue("Expected 0, got " + observations.get(1).getX(), observations.get(1).getX() == 0);

	}
	
	@Test
	public void testMultipleObservationsAtDifferentLocationsWithRotation()
	{
		// at position 20, observe point with relative distance of zero
		MovingLidarObservationBuffer buffer = new MovingLidarObservationBuffer();
		addObservation(buffer, 0, 0, 20, Vector3D.ZERO);
		addObservation(buffer, 90, 0, 11, Vector3D.ZERO);

		// move to position 10, point shoule now be observed at a relative
		// distance of 10 (20-10=10)
		List<LidarObservation> observations = buffer.getTranslatedObservations(
				new Rotation(RotationOrder.XYZ, 0, 0, 0), new Vector3D(0, 10, 0));
		assertTrue("Expected 10, got " + observations.get(0).getY(), observations.get(0).getY() == 10);
		assertTrue("Expected 0, got " + observations.get(0).getX(), observations.get(0).getX() == 0);

		assertTrue("Expected 9, got " + observations.get(1).getX(), observations.get(1).getX() == 9);
		assertTrue("Expected 10, got " + observations.get(1).getY(), observations.get(1).getY() == 10);

	}


	private void addObservation(MovingLidarObservationBuffer buffer, double worldAngle, double worldX, double worldY,
			Vector3D observation)
	{
		RobotLocation robotLocation = new RobotLocation();
		robotLocation.setHeading(new Angle(worldAngle, AngleUnits.DEGREES));
		robotLocation.setX(new Distance(worldX, DistanceUnit.CM));
		robotLocation.setY(new Distance(worldY, DistanceUnit.CM));

		List<LidarObservation> observations = new LinkedList<LidarObservation>();
		observations.add(new LidarObservation(observation,false));

		robotLocation.setObservations(observations);

		buffer.addLidarObservation(robotLocation);

	}

}
