package au.com.rsutton.mapping.particleFilter;

import java.util.List;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.navigation.feature.RobotLocationDeltaHelper;
import au.com.rsutton.navigation.feature.RobotLocationDeltaListener;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.ui.MapDrawingWindow;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.AngleUnits;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;

public class RobotPoseSourceDeadReconning implements RobotPoseSource, RobotLocationDeltaListener
{
	DistanceXY location = new DistanceXY(0, 0, DistanceUnit.CM);
	double heading;

	public RobotPoseSourceDeadReconning(RobotInterface robot)
	{
		robot.addMessageListener(this);
	}

	@Override
	public DistanceXY getXyPosition()
	{
		return location;
	}

	@Override
	public void onMessage(Angle deltaHeading, Distance deltaDistance, List<ScanObservation> robotLocation)
	{

		location = RobotLocationDeltaHelper.applyDelta(deltaHeading, deltaDistance,
				new Angle(heading, AngleUnits.DEGREES), location.getX(), location.getY());
		heading = HeadingHelper.normalizeHeading(heading + deltaHeading.getDegrees());

		// Vector3D move = new Vector3D(0,
		// deltaDistance.convert(DistanceUnit.MM), 0);
		// Rotation rotation = new Rotation(RotationOrder.XYZ, 0, 0,
		// Math.toRadians(heading));
		// Vector3D result = rotation.applyTo(move);
		// x += result.getX();
		// y += result.getY();
	}

	@Override
	public double getHeading()
	{
		return heading;
	}

	@Override
	public double getStdDev()
	{
		return 0;
	}

	@Override
	public void addDataSoures(MapDrawingWindow ui)
	{

	}

	@Override
	public void shutdown()
	{

	}
}
