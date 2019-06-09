package au.com.rsutton.robot.roomba;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.maschel.roomba.RoombaJSSC;
import com.maschel.roomba.RoombaJSSCSerial;
import com.maschel.roomba.song.RoombaNote;
import com.maschel.roomba.song.RoombaNoteDuration;
import com.maschel.roomba.song.RoombaSongNote;

import au.com.rsutton.config.Config;
import au.com.rsutton.hazelcast.DataLogLevel;
import au.com.rsutton.hazelcast.DataLogValue;
import au.com.rsutton.hazelcast.RobotTelemetry;
import au.com.rsutton.hazelcast.SetMotion;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.AngleUnits;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;

public class Roomba630 implements Runnable
{
	// configure platform.

	// publish deltas

	static final String ROOMBA_USB_PORT = "roomba usb port";
	private static final int ALARM_SONG = 0;
	public static final int STRAIGHT = 32768;
	// enact commands
	final private DistanceUnit distUnit = DistanceUnit.MM;
	final private TimeUnit timeUnit = TimeUnit.SECONDS;
	private RoombaJSSC roomba;

	Logger logger = LogManager.getLogger();

	final Object sync = new Object();

	int currentSpeed;
	int targetSpeed;
	int turnRadius;

	private volatile long commandExpires;
	private ScheduledFuture<?> future;

	Roomba630()
	{

	}

	void configure(Config config, String port) throws InterruptedException
	{
		synchronized (sync)
		{
			roomba = new RoombaJSSCSerial();

			String loadSetting = config.loadSetting(ROOMBA_USB_PORT, "/dev/ttyUSB0");
			System.out.println("Using " + loadSetting);
			roomba.connect(loadSetting);
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

			int ctr = 0;
			int batteryCharge = 0;
			while (batteryCharge < 12000)
			{
				ctr++;
				roomba.updateSensors();
				batteryCharge = roomba.batteryVoltage();
				System.out.println("Battery charge/capacity/voltage/temperature " + roomba.batteryCharge() + " "
						+ roomba.batteryCapacity() + " " + roomba.batteryVoltage() + " " + roomba.batteryTemperature());
				if (batteryCharge < 500)
				{
					System.out.println("Battery is too low to start (less than 500mah");
					TimeUnit.SECONDS.sleep(1);
				}
				if (ctr > 30)
				{
					shutdown();
					System.exit(-1);
					throw new RuntimeException("Out of here");
				}

			}

			roomba.safeMode();
			TimeUnit.SECONDS.sleep(1);
			roomba.leds(true, true, true, true, 100, 100);

			setupMusic();

			alarm();
		}
		future = Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(this, 200, 50,
				TimeUnit.MILLISECONDS);

	}

	private void setupMusic()
	{
		RoombaSongNote[] notes = {
				new RoombaSongNote(RoombaNote.C3, RoombaNoteDuration.SixteenthNote),
				new RoombaSongNote(RoombaNote.E3, RoombaNoteDuration.SixteenthNote),
				new RoombaSongNote(RoombaNote.C3, RoombaNoteDuration.SixteenthNote),
				new RoombaSongNote(RoombaNote.E3, RoombaNoteDuration.SixteenthNote) };
		// Save to song number 0, tempo (in BPM) 125
		roomba.song(ALARM_SONG, notes, 125);
		// Play song 0

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

	boolean getBumpLeft()
	{
		return bumpLeft;
	}

	boolean getBumpRight()
	{
		return bumpRight;
	}

	int batteryStats = 0;
	private boolean bumpLeft = false;
	private boolean bumpRight = false;

	@Override
	public void run()
	{

		// publish data

		try
		{

			synchronized (sync)
			{
				batteryStats++;

				roomba.updateSensors();
				if (batteryStats % 10 == 0 && roomba.mode() == 1)
				{
					System.out.println("Reconnecting to the roomba");
					roomba.safeMode();
				}

				updateAngleTurned();

				updateDistanceTraveled();

				bumpLeft = roomba.bumpLeft();
				bumpRight = roomba.bumpRight();

				RobotTelemetry location = new RobotTelemetry();
				location.setDistanceTravelled(getDistanceTraveled());
				location.setDeadReaconingHeading(getAngleTurned());
				location.setBumpLeft(getBumpLeft());
				location.setBumpRight(getBumpRight());
				location.publish();

				if (commandExpires > System.currentTimeMillis())
				{
					if (roomba.bumpLeft() || roomba.bumpRight())
					{
						// only allow reversing
						if (currentSpeed > 0 || targetSpeed > 0)
						{
							currentSpeed = 0;
							targetSpeed = 0;
							roomba.drive(0, 0);
						}
						if (targetSpeed < -50)
						{
							// top speed in reverse is 50mm/second
							targetSpeed = -50;
						}

					}
					if (targetSpeed > currentSpeed)
					{
						// forward or accelerating, ramp up
						currentSpeed += Math.min(50, targetSpeed - currentSpeed);
					}
					if (targetSpeed < currentSpeed)
					{
						// slowing down or reversing, go straight to the target
						// speed
						currentSpeed = targetSpeed;
					}
					logger.debug("current/target speed " + currentSpeed + " " + targetSpeed);

					roomba.drive(currentSpeed, turnRadius);

				} else
				{
					currentSpeed = 0;
					targetSpeed = 0;
					roomba.driveDirect(0, 0);
					System.out.println("Command is expired");
				}
				if (batteryStats % 10 == 0)
				{

					double percent = ((roomba.batteryVoltage() - 12000) / 3500d);
					DataLogLevel level = DataLogLevel.INFO;
					if (percent < 0.30)
					{
						level = DataLogLevel.WARN;
					}
					if (percent < 0.20)
					{
						level = DataLogLevel.ERROR;
					}

					new DataLogValue("Roomba-Battery V", "" + roomba.batteryVoltage(), DataLogLevel.INFO).publish();
					new DataLogValue("Roomba-Battery %", "" + percent, level).publish();

					System.out.println("Battery charge/capacity/voltage/temperature " + roomba.batteryCharge() + " "
							+ roomba.batteryCapacity() + " " + roomba.batteryVoltage() + " "
							+ roomba.batteryTemperature());

					if (roomba.batteryVoltage() < 12000)
					{
						// stop !!!
						roomba.driveDirect(0, 0);
						future.cancel(false);
						shutdown();
						System.out.println("Battery is flat");
						System.exit(-1);
					}
				}
			}

		} catch (

		Exception e)
		{
			e.printStackTrace();
		}

	}

	private void updateDistanceTraveled()
	{
		// I don't under stand why the roomba returns negative
		// distance
		// traveled values when travelling forwards
		int distanceTraveled = roomba.distanceTraveled();
		if (totalDistanceTraveled == null)
		{
			totalDistanceTraveled = (double) distanceTraveled;
		} else
		{
			totalDistanceTraveled += distanceTraveled;
		}

		new DataLogValue("Roomba-Distance Traveled", "" + distanceTraveled, DataLogLevel.INFO).publish();
		if (distanceTraveled != 0)
		{
			System.out.println("Distance Traveled: " + distanceTraveled);
		}
	}

	private void updateAngleTurned()
	{

		int angleTurned = roomba.angleTurned();
		if (totalAngleTurned == null)
		{
			totalAngleTurned = (double) angleTurned;
		} else
		{
			totalAngleTurned += angleTurned;
		}

		new DataLogValue("Roomba-Angle turned", "" + angleTurned, DataLogLevel.INFO).publish();
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
				currentSpeed = 0;
				targetSpeed = 0;
				roomba.driveDirect(0, 0);
			}
			new DataLogValue("Roomba-Speed", "0", DataLogLevel.WARN).publish();
		} else
		{

			int speed = (int) command.getSpeed().getSpeed(distUnit, timeUnit);

			// convert to MM
			int radius = (int) command.getTurnRadius() * 10;
			if (Math.abs(radius) >= STRAIGHT)
			{
				// because 32768 is a special value which is broken by * 10
				radius = STRAIGHT;
			}

			new DataLogValue("Roomba-Radius", "" + radius, DataLogLevel.INFO).publish();
			new DataLogValue("Roomba-Speed", "" + speed, DataLogLevel.INFO).publish();

			System.out.println("Set radius to " + radius + " speed " + speed);
			synchronized (sync)
			{

				targetSpeed = speed;
				turnRadius = radius;

			}
		}
	}

	double calculateRadiusToTurnAngleInDistance(double angle, Distance distance)
	{
		double circumference = (360 / angle) * distance.convert(DistanceUnit.MM);

		// c = 2*pi *r;

		double radius = circumference / (2.0 * Math.PI);

		return radius;
	}

	public void alarm()
	{
		roomba.play(ALARM_SONG);
	}

}
