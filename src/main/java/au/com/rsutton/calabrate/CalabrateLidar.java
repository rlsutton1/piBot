package au.com.rsutton.calabrate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.BrokenBarrierException;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.pi4j.gpio.extension.grovePi.GrovePiProvider;
import com.pi4j.gpio.extension.lidar.LidarScanner;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import au.com.rsutton.config.Config;
import au.com.rsutton.i2c.I2cSettings;
import au.com.rsutton.mapping.XY;
import au.com.rsutton.mapping.v3.impl.ObservedPoint;
import au.com.rsutton.mapping.v3.linearEquasion.LinearEquasionFactory;
import au.com.rsutton.mapping.v3.linearEquasion.LinearEquasionNormal;
import au.com.rsutton.robot.stepper.StepperMotor;

public class CalabrateLidar
{
	private LidarScanner lidar;
	volatile private double currentReading;

	public CalabrateLidar()
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

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Move robot 100cm from a wall, and press enter when done");
		br.read();
		currentReading = Vector3D.distance(Vector3D.ZERO, lidar.scanAngle(0));
		double reading1 = currentReading;

		System.out.println("Move robot 200cm from a wall, and press enter when done");
		br.read();
		currentReading = Vector3D.distance(Vector3D.ZERO, lidar.scanAngle(0));
		double reading2 = currentReading;

		LinearEquasionNormal lineEquasion = (LinearEquasionNormal) LinearEquasionFactory.getEquasion(
				new ObservedPoint(new XY(100, (int) reading1), null, null),
				new ObservedPoint(new XY(200, (int) reading2), null, null));

		int c = (int) lineEquasion.getC();
		double m = lineEquasion.getM();

		config.storeSetting("lidar.c", c);
		config.storeSetting("lidar.m", m);

		config.save();

		System.exit(0);

	}

}
