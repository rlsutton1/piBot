package au.com.rsutton.entryPoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.BrokenBarrierException;

import com.pi4j.gpio.extension.adafruit.ADS1115;
import com.pi4j.gpio.extension.adafruit.Adafruit16PwmProvider;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import au.com.rsutton.calabrate.CalabrateCompass;
import au.com.rsutton.calabrate.CalabrateHallEffect;
import au.com.rsutton.calabrate.CalabrateLidar;
import au.com.rsutton.calabrate.CalabrateLidarAngles;
import au.com.rsutton.calabrate.CalabrateLidarManual;
import au.com.rsutton.calabrate.CalabrateTelemetry;
import au.com.rsutton.calabrate.TestStepper;
import au.com.rsutton.i2c.I2cSettings;
import au.com.rsutton.robot.rover.Rover;

public class Main
{
	boolean distanceOk = true;

	public static void main(String[] args)
			throws InterruptedException, IOException, BrokenBarrierException, UnsupportedBusNumberException
	{
		// I2CFactory.setFactory(new I2CFactoryProviderBanana());

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("Press 0 to start the rover\n");
		System.out.println("Press 1 to calabrate compass");
		System.out.println("Press 2 to calabrate lidar");
		System.out.println("Press 3 to calabrate telemetry");
		System.out.println("Press 4 to test stepper");

		System.out.println("Press 5 to hall effect");
		System.out.println("Press 6 to calabrate lidar manually");
		System.out.println("Press 7 to calabrate lidar Angles");

		int ch = br.read();
		if (ch == '0')
		{
			new Rover();
			while (true)
			{
				Thread.sleep(1000);
			}
		} else if (ch == '1')
		{
			new CalabrateCompass();
		} else if (ch == '2')
		{
			new CalabrateLidar();
		} else if (ch == '3')
		{
			new CalabrateTelemetry();
		} else if (ch == '4')
		{
			new TestStepper();
		} else if (ch == '5')
		{
			new CalabrateHallEffect();
		} else if (ch == '6')
		{
			new CalabrateLidarManual();
		} else if (ch == '7')
		{
			new CalabrateLidarAngles();
		}

	}

	// private static void sonarFullScanTest() throws IOException,
	// InterruptedException
	// {
	// Adafruit16PwmProvider provider = setupPwm();
	//
	// ADS1115 ads = new ADS1115(1, 0x48);
	//
	// Sonar sonar = new Sonar(0.1, 2880, 0);
	// new FullScan(provider);
	//
	// }

	private static Adafruit16PwmProvider setupPwm()
			throws IOException, InterruptedException, UnsupportedBusNumberException
	{
		Adafruit16PwmProvider provider = new Adafruit16PwmProvider(I2cSettings.busNumber, 0x40);
		provider.setPWMFreq(30);
		return provider;
	}

	private static void sonarTest() throws IOException, InterruptedException, UnsupportedBusNumberException
	{
		ADS1115 ads = new ADS1115(I2cSettings.busNumber, 0x48);

		// Sonar sonar = new Sonar(0.1, 2880, 0);
		// ads.addListener(sonar);
		for (int i = 0; i < 5000; i++)
		{

			// int values = (int) sonar.getCurrentDistance().convert(
			// DistanceUnit.CM);
			// System.out.println("value: " + values);
			Thread.sleep(100);

		}
	}

}
