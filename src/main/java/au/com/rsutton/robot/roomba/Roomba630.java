package au.com.rsutton.robot.roomba;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.maschel.roomba.RoombaJSSC;
import com.maschel.roomba.RoombaJSSCSerial;

import au.com.rsutton.config.Config;
import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.hazelcast.DataLogValue;
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

			Runtime.getRuntime().addShutdownHook(new Thread()
			{
				@Override
				public void run()
				{
					shutdown();
				}
			});

			// Make roomba ready for communication & control (safe mode)
			roomba.start();
			TimeUnit.SECONDS.sleep(1);

			// check battery

			int batteryCharge = 0;
			while (batteryCharge < 12000)
			{
				roomba.updateSensors();
				batteryCharge = roomba.batteryVoltage();
				System.out.println("Battery charge/capacity/voltage/temperature " + roomba.batteryCharge() + " "
						+ roomba.batteryCapacity() + " " + roomba.batteryVoltage() + " " + roomba.batteryTemperature());
				if (batteryCharge < 500)
				{
					System.out.println("Battery is too low to start (less than 500mah");
					TimeUnit.SECONDS.sleep(1);
				}

			}

			roomba.safeMode();
			TimeUnit.SECONDS.sleep(1);
			roomba.leds(true, true, true, true, 100, 100);

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

	Double totalDistanceTraveled;
	Double totalAngleTurned;

	Distance getDistanceTraveled()
	{
		synchronized (sync)
		{

			return new Distance(totalDistanceTraveled, DistanceUnit.MM);
		}
	}

	Angle getAngleTurned()
	{
		synchronized (sync)
		{

			return new Angle(totalAngleTurned, AngleUnits.DEGREES);
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

				roomba.updateSensors();

				updateAngleTurned();

				updateDistanceTraveled();

				if (roomba.bumpLeft() || roomba.bumpRight())
				{
					roomba.drive(0, 0);
				}
				if (batteryStats % 10 == 0)
				{

					new DataLogValue("Roomba-Battery V", "" + roomba.batteryVoltage()).publish();
					new DataLogValue("Roomba-Battery %", "" + ((roomba.batteryVoltage() - 12000) / 7000d)).publish();

					System.out.println("Battery charge/capacity/voltage/temperature " + roomba.batteryCharge() + " "
							+ roomba.batteryCapacity() + " " + roomba.batteryVoltage() + " "
							+ roomba.batteryTemperature());

					if (roomba.batteryVoltage() < 12000)
					{
						System.out.println("Battery is flat");
						System.exit(-1);
					}
				}
			}

		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private void updateDistanceTraveled()
	{
		// I don't under stand why the roomba returns negative
		// distance
		// traveled values when travelling forwards
		int distanceTraveled = -roomba.distanceTraveled() * 10;
		if (totalDistanceTraveled == null)
		{
			totalDistanceTraveled = (double) distanceTraveled;
		} else
		{
			totalDistanceTraveled += distanceTraveled;
		}

		new DataLogValue("Roomba-Distance Traveled", "" + distanceTraveled).publish();
		if (distanceTraveled != 0)
		{
			System.out.println("Distance Traveled: " + distanceTraveled);
		}
	}

	private void updateAngleTurned()
	{
		int angleTurned = roomba.angleTurned() * 3;
		if (totalAngleTurned == null)
		{
			totalAngleTurned = (double) angleTurned;
		} else
		{
			totalAngleTurned += angleTurned;
		}

		new DataLogValue("Roomba-Angle turned", "" + angleTurned).publish();
		if (angleTurned != 0)
		{

			System.out.println("Angle turned: " + angleTurned);
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
			new DataLogValue("Roomba-Speed", "0").publish();
		} else
		{
			Double changeInHeading = command.getChangeHeading();
			changeInHeading = HeadingHelper.normalizeHeading(changeInHeading);
			if (changeInHeading > 180)
			{
				changeInHeading = -360 + changeInHeading;
			}

			int speed = (int) command.getSpeed().getSpeed(distUnit, timeUnit);

			int radius = STRAIGHT;
			if (changeInHeading > 1.0)
			{
				radius = (int) (2000.0 * ((45.0 - Math.min(44d, changeInHeading)) / 45.0));
			} else if (changeInHeading < -1.0)
			{
				radius = (int) (2000.0 * ((-45.0 - Math.max(-44d, changeInHeading)) / 45.0));
			}
			new DataLogValue("Roomba-Radius", "" + radius).publish();
			new DataLogValue("Roomba-Speed", "" + speed).publish();
			new DataLogValue("Roomba-Requested angle change", "" + changeInHeading).publish();

			System.out.println("Set radius to " + radius + " for " + changeInHeading + " speed " + speed);
			synchronized (sync)
			{
				if (!roomba.bumpLeft() && !roomba.bumpRight())
				{
					roomba.drive(speed, radius);
				} else
				{

					roomba.drive(0, 0);
					System.out.println("Stopping due to bumper");
				}
			}
		}
	}

}
