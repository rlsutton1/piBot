package au.com.rsutton.entryPoint;

import java.io.IOException;

import au.com.rsutton.entryPoint.controllers.ServoController;
import au.com.rsutton.entryPoint.sonar.FullScan;
import au.com.rsutton.entryPoint.sonar.Sonar;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.robot.rover.Rover;

import com.pi4j.gpio.extension.adafruit.ADS1115;
import com.pi4j.gpio.extension.adafruit.Adafruit16PwmPin;
import com.pi4j.gpio.extension.adafruit.Adafruit16PwmProvider;
import com.pi4j.gpio.extension.adafruit.PwmPin;
import com.pi4j.io.gpio.PinMode;

public class Main
{
	boolean distanceOk = true;

	public static void main(String[] args) throws InterruptedException,
			IOException
	{
		new Rover();
		while (true)
		{
			Thread.sleep(1000);
		}

	}



	private static void sonarFullScanTest() throws IOException,
			InterruptedException
	{
		Adafruit16PwmProvider provider = setupPwm();

		ADS1115 ads = new ADS1115(1, 0x48);

		Sonar sonar = new Sonar(0.1, 2880, 0);
		new FullScan(provider);

	}

	

	private static Adafruit16PwmProvider setupPwm() throws IOException,
			InterruptedException
	{
		Adafruit16PwmProvider provider = new Adafruit16PwmProvider(1, 0x40);
		provider.setPWMFreq(30);
		return provider;
	}

	private static void basicSonarTest()
	{
		try
		{

			Adafruit16PwmProvider provider = new Adafruit16PwmProvider(1, 0x40);

			provider.setPWMFreq(30);

			provider.export(Adafruit16PwmPin.GPIO_08, PinMode.PWM_OUTPUT);
			provider.export(Adafruit16PwmPin.GPIO_09, PinMode.PWM_OUTPUT);

			PwmPin servoPin1 = new PwmPin(provider, Adafruit16PwmPin.GPIO_08);
			PwmPin servoPin2 = new PwmPin(provider, Adafruit16PwmPin.GPIO_09);

			ServoController controller1 = new ServoController(servoPin1, 81,
					307, 1);
			ServoController controller2 = new ServoController(servoPin2, 81,
					307, 1);
			int[] pos = new int[] {
					-60, -28, 4, 36, 70, 36, 4, -28 };

			ADS1115 ads = new ADS1115(1, 0x48);

			Sonar sonar = new Sonar(0.1, 2880, 0);
			ads.addListener(sonar);

			while (true)
			{
				for (int i = 0; i < 1000000; i++)
				{
					int idx = i % pos.length;
					int p = pos[idx];
					controller1.setOutput(p);
					controller2.setOutput(p);
					Thread.sleep(300);
					int values = (int) sonar.getCurrentDistance().convert(
							DistanceUnit.CM);
					System.out.println("value: " + values);

				}
			}

		} catch (IOException | InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally
		{
			System.out.println("turn off servos");

		}
	}

	private static void sonarTest() throws IOException, InterruptedException
	{
		ADS1115 ads = new ADS1115(1, 0x48);

		Sonar sonar = new Sonar(0.1, 2880, 0);
		ads.addListener(sonar);
		for (int i = 0; i < 5000; i++)
		{

			int values = (int) sonar.getCurrentDistance().convert(
					DistanceUnit.CM);
			System.out.println("value: " + values);
			Thread.sleep(100);

		}
	}

}
