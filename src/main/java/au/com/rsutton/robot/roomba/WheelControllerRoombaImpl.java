package au.com.rsutton.robot.roomba;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.maschel.roomba.RoombaJSSC;

import au.com.rsutton.config.Config;
import au.com.rsutton.robot.rover.WheelController;
import au.com.rsutton.robot.rover5.QuadratureToDistance;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;
import au.com.rsutton.units.Speed;

public class WheelControllerRoombaImpl implements WheelController
{

	final QuadratureToDistance distanceConverter;
	final private DistanceUnit distUnit = DistanceUnit.MM;
	final private TimeUnit timeUnit = TimeUnit.SECONDS;
	private RoombaJSSC roomba;

	public WheelControllerRoombaImpl(RoombaJSSC roomba, double deadZone, Config config, String wheelLabel)
			throws IOException
	{

		this.roomba = roomba;
		distanceConverter = new QuadratureToDistance(config, wheelLabel);

	}

	/**
	 * set the speed that this wheel should travel at
	 * 
	 * @param speed
	 */
	@Override
	public void setSpeed(Speed leftSpeed, Speed rightSpeed)
	{

		roomba.driveDirect((int) rightSpeed.getSpeed(distUnit, timeUnit), (int) leftSpeed.getSpeed(distUnit, timeUnit));
	}

	/**
	 * get the distance covered by this wheel
	 * 
	 * @return
	 */
	@Override
	public Distance getDistanceLeftWheel()
	{

		return distanceConverter.scale(roomba.encoderCountsLeft());

	}

	@Override
	public Distance getDistanceRightWheel()
	{

		return distanceConverter.scale(roomba.encoderCountsRight());

	}

}
