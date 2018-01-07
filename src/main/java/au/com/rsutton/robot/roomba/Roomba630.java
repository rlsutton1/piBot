package au.com.rsutton.robot.roomba;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.maschel.roomba.RoombaJSSC;
import com.maschel.roomba.RoombaJSSCSerial;

import au.com.rsutton.config.Config;
import au.com.rsutton.hazelcast.SetMotion;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.AngleUnits;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;

public class Roomba630 implements Runnable
{
	// configure platform.

	// publish deltas

	private static final int STRAIGHT = 32768;
	// enact commands
	final private DistanceUnit distUnit = DistanceUnit.MM;
	final private TimeUnit timeUnit = TimeUnit.SECONDS;
	private RoombaJSSC roomba;

	final Object sync = new Object();

	private volatile long commandExpires;

	Roomba630()
	{

	}

	void configure(Config config) throws InterruptedException
	{
		synchronized (sync)
		{
			roomba = new RoombaJSSCSerial();

			roomba.connect(config.loadSetting("roomba usb port", "/dev/ttyUSB0"));
			// Use
			// portList()
			// to
			// get
			// available

			// Make roomba ready for communication & control (safe mode)
			roomba.startup();
			TimeUnit.SECONDS.sleep(1);
			roomba.safeMode();
			TimeUnit.SECONDS.sleep(1);
			roomba.leds(true, true, true, true, 100, 100);

			Runtime.getRuntime().addShutdownHook(new Thread()
			{
				@Override
				public void run()
				{
					shutdown();
				}
			});
		}
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this, 200, 100, TimeUnit.MILLISECONDS);

	}

	void shutdown()
	{
		synchronized (sync)
		{
			// Return to normal (human control) mode
			roomba.stop();

			// Close serial connection
			roomba.disconnect();
		}
	}

	Distance getDistanceTraveled()
	{
		synchronized (sync)
		{

			return new Distance(roomba.distanceTraveled(), DistanceUnit.MM);
		}
	}

	Angle getAngleTurned()
	{
		synchronized (sync)
		{
			return new Angle(roomba.angleTurned(), AngleUnits.DEGREES);
		}
	}

	int batteryStats = 0;

	@Override
	public void run()
	{

		// publish data

		try
		{

			if (commandExpires < System.currentTimeMillis())
			{
				synchronized (sync)
				{
					roomba.driveDirect(0, 0);
				}
			}

			synchronized (sync)
			{
				batteryStats++;
				if (batteryStats % 10 == 0)
				{
					roomba.updateSensors();
					System.out.println("Battery charge/capacity/voltage/temperature " + roomba.batteryCharge() + " "
							+ roomba.batteryCapacity() + " " + roomba.batteryVoltage() + " "
							+ roomba.batteryTemperature());
				}
			}

		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public void setMotion(SetMotion command)
	{
		commandExpires = System.currentTimeMillis() + 2000;

		if (command.getFreeze())
		{
			synchronized (sync)
			{

				roomba.driveDirect(0, 0);
			}
		} else
		{
			Double changeInHeading = command.getChangeHeading();

			int speed = (int) command.getSpeed().getSpeed(distUnit, timeUnit);

			int radius = STRAIGHT;
			if (changeInHeading > 1.0)
			{
				radius = (int) (2000.0 * ((90.0 - Math.min(90d, changeInHeading)) / 90.0));
			} else if (changeInHeading < -1.0)
			{
				radius = (int) (2000.0 * ((-90.0 - Math.max(-90d, changeInHeading)) / 90.0));
			}
			System.out.println("Set radius to " + radius + " for " + changeInHeading);
			synchronized (sync)
			{
				roomba.drive(speed, radius);
			}
		}
	}

}
