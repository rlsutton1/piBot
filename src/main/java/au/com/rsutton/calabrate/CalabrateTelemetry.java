package au.com.rsutton.calabrate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.BrokenBarrierException;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.config.Config;
import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;
import au.com.rsutton.i2c.I2cSettings;
import au.com.rsutton.robot.rover.WheelController;
import au.com.rsutton.robot.rover.WheelFactory;
import au.com.rsutton.robot.stepper.StepperMotor;

import com.pi4j.gpio.extension.adafruit.AdafruitPCA9685;
import com.pi4j.gpio.extension.grovePi.GrovePiPin;
import com.pi4j.gpio.extension.grovePi.GrovePiProvider;
import com.pi4j.gpio.extension.lidar.LidarScanner;
import com.pi4j.io.gpio.PinMode;

public class CalabrateTelemetry
{
	private GrovePiProvider grove;
	// private CompassLSM303 compass;
	private WheelController rightWheel;
	private WheelController leftWheel;

	// private SpeedHeadingController speedHeadingController;

	public CalabrateTelemetry() throws IOException, InterruptedException, BrokenBarrierException
	{

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("Position the robot about 2 meters from a wall");
		System.out.println("Press any key when ready");
		br.read();

		Config config = new Config();

		grove = new GrovePiProvider(I2cSettings.busNumber, 4);

		grove.setMode(GrovePiPin.GPIO_A1, PinMode.ANALOG_INPUT);

		// compass = new CompassLSM303(config);

		rightWheel = WheelFactory.setupRightWheel(grove, config);

		leftWheel = WheelFactory.setupLeftWheel(grove, config);

//		AdafruitPCA9685 pwm = new AdafruitPCA9685(); // 0x40 is the default
//		// address
//		pwm.setPWMFreq(60); // Set frequency to 60 Hz

		StepperMotor driver = new StepperMotor(8);

		LidarScanner lidar = new LidarScanner(driver, config, grove);

		System.out.println("Please wait scanning...");

		Thread.sleep(1000);

		double reading1 = (Vector3D.distance(Vector3D.ZERO, lidar.scan(0)) + Vector3D
				.distance(Vector3D.ZERO, lidar.scan(0))) / 2.0;

		// float heading = compass.getHeading();

		// speedHeadingController = new SpeedHeadingController(rightWheel,
		// leftWheel, heading);

		Thread.sleep(5000);

		Distance initialRight = rightWheel.getDistance();
		Distance initialLeft = leftWheel.getDistance();

		rightWheel.setSpeed(new Speed(new Distance(10, DistanceUnit.CM), Time
				.perSecond()));

		leftWheel.setSpeed(new Speed(new Distance(10, DistanceUnit.CM), Time
				.perSecond()));

		System.out.println("driving");

		// SetMotion motion = new SetMotion();
		// motion.setHeading((double) heading);
		// motion.setSpeed(new Speed(new Distance(10, DistanceUnit.CM), Time
		// .perSecond()));
		// speedHeadingController.setDesiredMotion(motion);

		while (rightWheel.getDistance().convert(DistanceUnit.CM) < 30)
		{
			Thread.sleep(100);
		}

		System.out.println("Stopped");

		rightWheel.setSpeed(new Speed(new Distance(0, DistanceUnit.CM), Time
				.perSecond()));

		leftWheel.setSpeed(new Speed(new Distance(0, DistanceUnit.CM), Time
				.perSecond()));

		// speedHeadingController.setDesiredMotion(motion);

		System.out.println("Waiting for settling");
		Thread.sleep(10000);

		Thread.sleep(1000);

		double reading2 = (Vector3D.distance(Vector3D.ZERO, lidar.scan(0)) + Vector3D
				.distance(Vector3D.ZERO, lidar.scan(0))) / 2.0;

		double leftRatio = (reading1 - reading2)
				/ (leftWheel.getDistance().convert(DistanceUnit.CM) - initialLeft
						.convert(DistanceUnit.CM));
		config.storeSetting("Quadrature.left",
				leftRatio * config.loadSetting("Quadrature.left", 1.0));
		double rightRatio = (reading1 - reading2)
				/ (rightWheel.getDistance().convert(DistanceUnit.CM) - initialRight
						.convert(DistanceUnit.CM));
		config.storeSetting("Quadrature.right",
				rightRatio * config.loadSetting("Quadrature.right", 1.0));

		config.save();

		System.exit(0);

	}
}
