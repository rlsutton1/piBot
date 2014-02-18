package au.com.rsutton.entryPoint.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.robot.HeadingProvider;

import com.pi4j.gpio.extension.adafruit.GyroProvider;

public class VehicleHeadingController implements Runnable
{

	private HBridgeController left;
	private HBridgeController right;
	private HeadingProvider headingProvider;
	volatile private int setHeading;
	private VehicleSpeedController vsc;
	private Pid pid;

	public VehicleHeadingController(HBridgeController left,
			HBridgeController right, HeadingProvider headingProvider,HeadingProvider gyro) throws IOException,
			InterruptedException
	{
		this.left = left;
		this.right = right;
		this.headingProvider = headingProvider;
		waitForGyro(gyro);
	}

	public void loadConfig() throws IOException
	{
		loadCalibrationData();
		vsc = new VehicleSpeedController(left, right);
		pid = new Pid(1, .10, .1, 100, 50, -50, true);
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this,
				100, 100, TimeUnit.MILLISECONDS);

	}

	public void autoConfigure(GyroProvider gyro) throws InterruptedException, IOException
	{
		waitForGyro(gyro);

		VehicleControlerCalibrator configurator = new VehicleControlerCalibrator(
				left, right, gyro);
		configurator.autoConfigure();

	}

	private void waitForGyro(HeadingProvider gyro) throws InterruptedException
	{
		left.setOutput(0);
		right.setOutput(0);// stopped

		while (gyro.isCalabrated() == false)
		{
			Thread.sleep(100);
			System.out.println("Waiting for gyro to calabrate");
		}
		Thread.sleep(1000);
		left.setOutput(0);
		right.setOutput(0);// stopped
	}

	public void setSpeed(Speed speedPercent) throws InterruptedException
	{
		vsc.setSpeed(speedPercent);
	}

	public void stop()
	{
		vsc.stop();
	}

	public void setHeading(int degrees)
	{
		setHeading = degrees;
	}

	private void loadCalibrationData() throws IOException
	{
		Properties props = new Properties();

		FileInputStream stream = null;
		try
		{
			System.out.println("loading...");
			stream = new FileInputStream(new File("GyroServoController.props"));
			props.load(stream);

			// nb: always set direction before center.
			left.setDirection(Integer.parseInt((String) props
					.get("leftDirection")));
			right.setDirection(Integer.parseInt((String) props
					.get("rightDirection")));

			System.out.println("right " + right.getDirection());
			System.out.println("left " + left.getDirection());

		} finally
		{
			if (stream != null)
			{
				stream.close();
			}
		}
	}

	@Override
	public void run()
	{
		// System.out.println("\f");
		// System.out.println("actual heading "+gyro.getZ()+" setHeading "+setHeading);

		double changeInHeading = HeadingHelper.getChangeInHeading(headingProvider.getHeading(),
				setHeading);


		if (vsc.getSetSpeed().getSpeed(DistanceUnit.MM, TimeUnit.SECONDS) > 4
				|| vsc.getSetSpeed()
						.getSpeed(DistanceUnit.MM, TimeUnit.SECONDS) < -4
				|| headingProvider.getHeading() > setHeading + 5 || headingProvider.getHeading() < setHeading - 5)
		{
			// we're either moving or our heading is off by more than 1 degree
			vsc.setDirectionAdjustment(pid.computePid(0, changeInHeading));
		} else
		{
			// allow the pid to compute so it doesnt build up an error
			pid.computePid(0, changeInHeading);
			// motors off!
			vsc.setDirectionAdjustment(0);
		}
	}

	public Speed getSetSpeed()
	{
		// TODO Auto-generated method stub
		return vsc.getSetSpeed();
	}

	public void reportActualSpeed(Speed speed)
	{
		vsc.setActualSpeed(speed);

	}
}
