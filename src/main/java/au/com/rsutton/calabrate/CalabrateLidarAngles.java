package au.com.rsutton.calabrate;

import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;

import com.pi4j.gpio.extension.grovePi.GrovePiProvider;
import com.pi4j.gpio.extension.lidar.LidarScanner;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import au.com.rsutton.config.Config;
import au.com.rsutton.i2c.I2cSettings;
import au.com.rsutton.robot.stepper.StepperMotor;

public class CalabrateLidarAngles
{
	private LidarScanner lidar;
	volatile private double currentReading;

	public CalabrateLidarAngles()
			throws IOException, InterruptedException, BrokenBarrierException, UnsupportedBusNumberException
	{
		Config config = new Config();

		// AdafruitPCA9685 pwm = new AdafruitPCA9685(); // 0x40 is the default
		// // address
		// pwm.setPWMFreq(60); // Set frequency to 60 Hz
		GrovePiProvider grove = new GrovePiProvider(I2cSettings.busNumber, 4);

		StepperMotor driver = new StepperMotor(8);

		config.loadSetting("lidar.c", 0);

		config.loadSetting("lidar.m", 1.0);

		lidar = new LidarScanner(driver, config, grove);

		lidar.scanAngle(-90);
		System.out.println("-90 degrees");
		Thread.sleep(10000);

		lidar.scanAngle(0);
		System.out.println("0 degrees");
		Thread.sleep(10000);

		lidar.scanAngle(90);
		System.out.println("90 degrees");
		Thread.sleep(10000);

		System.exit(0);

	}

}
