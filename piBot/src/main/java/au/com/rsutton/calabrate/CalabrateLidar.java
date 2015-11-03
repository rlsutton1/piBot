package au.com.rsutton.calabrate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.config.Config;
import au.com.rsutton.mapping.XY;
import au.com.rsutton.mapping.v3.impl.ObservedPoint;
import au.com.rsutton.mapping.v3.linearEquasion.LinearEquasion;
import au.com.rsutton.mapping.v3.linearEquasion.LinearEquasionFactory;
import au.com.rsutton.mapping.v3.linearEquasion.LinearEquasionNormal;
import au.com.rsutton.robot.rover.WheelController;

import com.pi4j.gpio.extension.adafruit.AdafruitPCA9685;
import com.pi4j.gpio.extension.grovePi.GrovePiProvider;
import com.pi4j.gpio.extension.lidar.Lidar;
import com.pi4j.gpio.extension.lidar.LidarDataListener;
import com.pi4j.gpio.extension.lsm303.CompassLSM303;

public class CalabrateLidar implements LidarDataListener
{
	private Lidar lidar;
	volatile private double currentReading;

	CalabrateLidar() throws IOException, InterruptedException
	{
		Config config = new Config();

		AdafruitPCA9685 pwm = new AdafruitPCA9685(); // 0x40 is the default
		// address
		pwm.setPWMFreq(60); // Set frequency to 60 Hz

		lidar = new Lidar(pwm, this, config);

		lidar.prepareForCalabration();

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out
				.println("Move robot 100cm from a wall, and press enter when done");
		br.read();
		currentReading = -1;
		System.out.println("Please wait...");
		while (currentReading == -1)
		{
			Thread.sleep(100);

		}
		double reading1 = currentReading;

		System.out
				.println("Move robot 200cm from a wall, and press enter when done");
		br.read();
		currentReading = -1;
		System.out.println("Please wait...");
		while (currentReading == -1)
		{
			Thread.sleep(100);

		}
		double reading2 = currentReading;

		LinearEquasionNormal lineEquasion = (LinearEquasionNormal) LinearEquasionFactory
				.getEquasion(new ObservedPoint(new XY(100, (int) reading1),
						null, null), new ObservedPoint(new XY(200,
						(int) reading2), null, null));

		int c = (int) lineEquasion.getC();
		double m = lineEquasion.getM();

		lidar.saveConfig(m,c);

		config.save();

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
