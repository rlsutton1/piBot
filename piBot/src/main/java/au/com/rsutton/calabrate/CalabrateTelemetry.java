package au.com.rsutton.calabrate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.config.Config;
import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;
import au.com.rsutton.hazelcast.SetMotion;
import au.com.rsutton.i2c.I2cSettings;
import au.com.rsutton.robot.rover.SpeedHeadingController;
import au.com.rsutton.robot.rover.WheelController;
import au.com.rsutton.robot.rover.WheelFactory;

import com.pi4j.gpio.extension.adafruit.AdafruitPCA9685;
import com.pi4j.gpio.extension.grovePi.GrovePiPin;
import com.pi4j.gpio.extension.grovePi.GrovePiProvider;
import com.pi4j.gpio.extension.lidar.Lidar;
import com.pi4j.gpio.extension.lidar.LidarDataListener;
import com.pi4j.gpio.extension.lsm303.CompassLSM303;
import com.pi4j.io.gpio.PinMode;

public class CalabrateTelemetry implements LidarDataListener
{
	private GrovePiProvider grove;
	private CompassLSM303 compass;
	private WheelController rightWheel;
	private WheelController leftWheel;
	private SpeedHeadingController speedHeadingController;
	private double currentReading;

	public CalabrateTelemetry() throws IOException, InterruptedException
	{
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("Position the robot about 2 meters from a wall");
		System.out.println("Press any key when ready");
		br.read();
		
		Config config = new Config();

		grove = new GrovePiProvider(I2cSettings.busNumber, 4);

		grove.setMode(GrovePiPin.GPIO_A1, PinMode.ANALOG_INPUT);

		compass = new CompassLSM303(config);

		rightWheel = WheelFactory.setupRightWheel(grove, config);

		leftWheel = WheelFactory.setupLeftWheel(grove, config);

		AdafruitPCA9685 pwm = new AdafruitPCA9685(); // 0x40 is the default
		// address
		pwm.setPWMFreq(60); // Set frequency to 60 Hz

		new Lidar(pwm, this, config);

		currentReading = -1;
		System.out.println("Please wait scanning...");
		while (currentReading == -1)
		{
			Thread.sleep(100);

		}
		double reading1 = currentReading;

		speedHeadingController = new SpeedHeadingController(rightWheel,
				leftWheel, compass.getHeading());

		Distance initialRight = rightWheel.getDistance();
		Distance initialLeft = leftWheel.getDistance();

		SetMotion motion = new SetMotion();
		motion.setHeading((double) compass.getHeading());
		motion.setSpeed(new Speed(new Distance(10, DistanceUnit.CM), Time
				.perSecond()));
		speedHeadingController.setDesiredMotion(motion);

		while (rightWheel.getDistance().convert(DistanceUnit.CM) < 100)
		{
			Thread.sleep(100);
		}

		motion.setSpeed(new Speed(new Distance(0, DistanceUnit.CM), Time
				.perSecond()));

		speedHeadingController.setDesiredMotion(motion);

		System.out.println("Waiting for settling");
		Thread.sleep(10000);

		currentReading = -1;
		System.out.println("Please wait...");
		while (currentReading == -1)
		{
			Thread.sleep(100);

		}
		double reading2 = currentReading;

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

		System.exit(0);

	}

	@Override
	public void addLidarData(Vector3D vector, double distanceCm,
			double angleDegrees)
	{
		if (Math.abs(angleDegrees) < 1)
		{
			currentReading = distanceCm;
			System.out.println("Reading accuired " + distanceCm);
		}

	}
}
