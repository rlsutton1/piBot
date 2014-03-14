package au.com.rsutton.entryPoint;

import java.io.IOException;

import au.com.rsutton.entryPoint.controllers.HBridgeController;
import au.com.rsutton.entryPoint.controllers.ServoController;
import au.com.rsutton.entryPoint.controllers.VehicleHeadingController;
import au.com.rsutton.entryPoint.quadrature.QuadratureEncoding;
import au.com.rsutton.entryPoint.sonar.FullScan;
import au.com.rsutton.entryPoint.sonar.Sonar;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.robot.rover.Rover;

import com.pi4j.gpio.extension.adafruit.ADS1115;
import com.pi4j.gpio.extension.adafruit.Adafruit16PwmPin;
import com.pi4j.gpio.extension.adafruit.Adafruit16PwmProvider;
import com.pi4j.gpio.extension.adafruit.GyroProvider;
import com.pi4j.gpio.extension.adafruit.PwmPin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.RaspiPin;

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

	private static void testQuadrature() throws IOException,
			InterruptedException
	{
		Adafruit16PwmProvider provider = setupPwm();
		provider.export(Adafruit16PwmPin.GPIO_00, PinMode.PWM_OUTPUT);
		provider.export(Adafruit16PwmPin.GPIO_01, PinMode.PWM_OUTPUT);
		provider.export(Adafruit16PwmPin.GPIO_04, PinMode.PWM_OUTPUT);
		provider.export(Adafruit16PwmPin.GPIO_05, PinMode.PWM_OUTPUT);

		PwmPin leftServoPin = new PwmPin(provider, Adafruit16PwmPin.GPIO_01);
		PwmPin leftDirectionPin = new PwmPin(provider, Adafruit16PwmPin.GPIO_00);

		PwmPin rightServoPin = new PwmPin(provider, Adafruit16PwmPin.GPIO_04);
		PwmPin rightDirectionPin = new PwmPin(provider,
				Adafruit16PwmPin.GPIO_05);

		HBridgeController leftServo = new HBridgeController(leftServoPin,
				leftDirectionPin, false);
		HBridgeController rightServo = new HBridgeController(rightServoPin,
				rightDirectionPin, false);

		leftServo.setOutput(0);
		rightServo.setOutput(0);

		Thread.sleep(2000);

		leftServo.setOutput(.03);
		rightServo.setOutput(.03);

		final GpioController gpio = GpioFactory.getInstance();

		// LHS
		// 04 = 23 - ok
		// 05 = 24 - ok

		// RHS
		// 03 = 22 - ok
		// 02 = 27 - ok

		new QuadratureEncoding(RaspiPin.GPIO_03, RaspiPin.GPIO_02, false);

		System.out.println("Ready");
		Thread.sleep(60000);

	}

	private static void sonarFullScanTest() throws IOException,
			InterruptedException
	{
		Adafruit16PwmProvider provider = setupPwm();

		ADS1115 ads = new ADS1115(1, 0x48);

		Sonar sonar = new Sonar(0.1, 2880, 0);
		new FullScan(provider);

	}

	private static VehicleHeadingController setupVehicleController(
			GyroProvider gyro, Adafruit16PwmProvider provider)
			throws IOException, InterruptedException
	{
		provider.export(Adafruit16PwmPin.GPIO_00, PinMode.PWM_OUTPUT);
		provider.export(Adafruit16PwmPin.GPIO_01, PinMode.PWM_OUTPUT);
		provider.export(Adafruit16PwmPin.GPIO_04, PinMode.PWM_OUTPUT);
		provider.export(Adafruit16PwmPin.GPIO_05, PinMode.PWM_OUTPUT);

		PwmPin leftServoPin = new PwmPin(provider, Adafruit16PwmPin.GPIO_01);
		PwmPin leftDirectionPin = new PwmPin(provider, Adafruit16PwmPin.GPIO_00);

		PwmPin rightServoPin = new PwmPin(provider, Adafruit16PwmPin.GPIO_05);
		PwmPin rightDirectionPin = new PwmPin(provider,
				Adafruit16PwmPin.GPIO_04);

		HBridgeController leftServo = new HBridgeController(leftServoPin,
				leftDirectionPin, false);
		HBridgeController rightServo = new HBridgeController(rightServoPin,
				rightDirectionPin, false);

		leftServo.setOutput(0);
		rightServo.setOutput(0);

		VehicleHeadingController controller = new VehicleHeadingController(
				leftServo, rightServo, gyro, gyro);

		controller.autoConfigure(gyro);
		return controller;
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
