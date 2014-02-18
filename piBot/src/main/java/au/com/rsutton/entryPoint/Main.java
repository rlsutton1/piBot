package au.com.rsutton.entryPoint;

import java.io.IOException;

import au.com.rsutton.entryPoint.controllers.HBridgeController;
import au.com.rsutton.entryPoint.controllers.ServoController;
import au.com.rsutton.entryPoint.controllers.VehicleHeadingController;
import au.com.rsutton.entryPoint.quadrature.QuadratureEncoding;
import au.com.rsutton.entryPoint.sonar.FullScan;
import au.com.rsutton.entryPoint.sonar.Sonar;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.robot.Robot;

import com.pi4j.gpio.extension.adafruit.ADS1115;
import com.pi4j.gpio.extension.adafruit.Adafruit16PwmPin;
import com.pi4j.gpio.extension.adafruit.Adafruit16PwmProvider;
import com.pi4j.gpio.extension.adafruit.GyroProvider;
import com.pi4j.gpio.extension.adafruit.PwmPin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.wiringpi.Spi;

public class Main
{
	boolean distanceOk = true;

	public static void main(String[] args) throws InterruptedException,
			IOException
	{
		new Robot();
		while (true)
		{
			Thread.sleep(1000);
		}

		// Camra c= new Camra();
		// c.run();

		// (new Controller()).start();

		// sonarFullScanTest();
		// testQuadrature();

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
				leftDirectionPin, ServoController.NORMAL);
		HBridgeController rightServo = new HBridgeController(rightServoPin,
				rightDirectionPin, ServoController.NORMAL);

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
		// GpioPinDigitalInput counterOverflowPin =
		// gpio.provisionDigitalInputPin(
		// RaspiPin.GPIO_05, PinPullResistance.PULL_DOWN);

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

	private static VehicleHeadingController setupVehicleController(GyroProvider gyro,
			Adafruit16PwmProvider provider) throws IOException,
			InterruptedException
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
				leftDirectionPin, ServoController.NORMAL);
		HBridgeController rightServo = new HBridgeController(rightServoPin,
				rightDirectionPin, ServoController.NORMAL);

		leftServo.setOutput(0);
		rightServo.setOutput(0);

		VehicleHeadingController controller = new VehicleHeadingController(leftServo,
				rightServo, gyro,gyro);

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

	/**
	 * @param args
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void doingLaps()
	{
//		HBridgeController leftServo = null;
//		HBridgeController rightServo = null;
//
//		try
//		{
//			Thread.sleep(2000);
//			GyroProvider gyro = new GyroProvider(1, 0x69);
//
//			Adafruit16PwmProvider provider = setupPwm();
//
//			provider.export(Adafruit16PwmPin.GPIO_00, PinMode.PWM_OUTPUT);
//			provider.export(Adafruit16PwmPin.GPIO_01, PinMode.PWM_OUTPUT);
//			provider.export(Adafruit16PwmPin.GPIO_04, PinMode.PWM_OUTPUT);
//			provider.export(Adafruit16PwmPin.GPIO_05, PinMode.PWM_OUTPUT);
//
//			PwmPin leftServoPin = new PwmPin(provider, Adafruit16PwmPin.GPIO_00);
//			PwmPin leftDirectionPin = new PwmPin(provider,
//					Adafruit16PwmPin.GPIO_01);
//
//			PwmPin rightServoPin = new PwmPin(provider,
//					Adafruit16PwmPin.GPIO_04);
//			PwmPin rightDirectionPin = new PwmPin(provider,
//					Adafruit16PwmPin.GPIO_05);
//
//			leftServo = new HBridgeController(leftServoPin, leftDirectionPin,
//					ServoController.NORMAL);
//			rightServo = new HBridgeController(rightServoPin,
//					rightDirectionPin, ServoController.NORMAL);
//
//			leftServo.setOutput(0);
//			rightServo.setOutput(0);
//
//			VehicleController controller = new VehicleController(leftServo,
//					rightServo, gyro);
//
//			// controller.autoConfigure();
//			// controller.stop();
//			// return;
//			controller.loadConfig();
//
//			int turn = 194;
//			int currentHeading = 0;
//			for (int i = 1; i < 7; i++)
//			{
//				controller.setSpeed(100);
//				Thread.sleep(10000);
//				controller.setSpeed(0);
//				for (; currentHeading < i * turn; currentHeading += 1)
//				{
//					controller.setHeading(currentHeading);
//					Thread.sleep(20);
//				}
//				Thread.sleep(500);
//			}
//
//			controller.setSpeed(0);
//
//			controller.stop();
//
//		} catch (IOException | InterruptedException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} finally
//		{
//			System.out.println("turn off servos");
//			leftServo.setOutput(0);
//			rightServo.setOutput(0);
//		}
//
		// gyroCalabrate();
	}

	private static void pwmTesting() throws IOException, InterruptedException
	{
		ServoController leftServo = null;
		Adafruit16PwmProvider provider = new Adafruit16PwmProvider(1, 0x40);

		provider.setPWMFreq(30);

		provider.export(Adafruit16PwmPin.GPIO_00, PinMode.PWM_OUTPUT);
		provider.export(Adafruit16PwmPin.GPIO_01, PinMode.PWM_OUTPUT);
		provider.export(Adafruit16PwmPin.GPIO_02, PinMode.PWM_OUTPUT);

		PwmPin leftServoPin = new PwmPin(provider, Adafruit16PwmPin.GPIO_00);
		PwmPin rightServoPin = new PwmPin(provider, Adafruit16PwmPin.GPIO_01);
		PwmPin controlPin = new PwmPin(provider, Adafruit16PwmPin.GPIO_02);

		leftServo = new ServoController(leftServoPin, 2048, 4095,
				ServoController.NORMAL);
		ServoController rightServo = new ServoController(rightServoPin, 2048,
				4095, ServoController.NORMAL);
		ServoController controlServo = new ServoController(controlPin, 2048,
				4095, ServoController.NORMAL);

		controlServo.setOutput(-15);
		Thread.sleep(500);
		leftServo.setOutput(-15);
		Thread.sleep(500);
		rightServo.setOutput(-15);

		for (double r = -15; r < 15; r += .05)
		{
			leftServo.setOutput(r);
			rightServo.setOutput(r);
			controlServo.setOutput(r);
			Thread.sleep(100);
			System.out.println(r);
		}
		leftServo.turnOff();
		controlServo.turnOff();
		rightServo.turnOff();
		// adafruitpwm();
		// gyroCalabrate();
	}

	private static void gyroCalabrate()
	{

		HBridgeController leftServo = null;
		HBridgeController rightServo = null;

		try
		{
			Thread.sleep(20000);
			GyroProvider gyro = new GyroProvider(1, 0x69);

			Adafruit16PwmProvider provider = setupPwm();

			provider.export(Adafruit16PwmPin.GPIO_00, PinMode.PWM_OUTPUT);
			provider.export(Adafruit16PwmPin.GPIO_01, PinMode.PWM_OUTPUT);

			PwmPin leftServoPin = new PwmPin(provider, Adafruit16PwmPin.GPIO_00);
			PwmPin leftDirectionPin = new PwmPin(provider,
					Adafruit16PwmPin.GPIO_01);

			PwmPin rightServoPin = new PwmPin(provider,
					Adafruit16PwmPin.GPIO_04);
			PwmPin rightDirectionPin = new PwmPin(provider,
					Adafruit16PwmPin.GPIO_05);

			leftServo = new HBridgeController(leftServoPin, leftDirectionPin,
					ServoController.NORMAL);
			rightServo = new HBridgeController(rightServoPin,
					rightDirectionPin, ServoController.NORMAL);

			VehicleHeadingController controller = new VehicleHeadingController(leftServo,
					rightServo, gyro,gyro);

			controller.autoConfigure(gyro);

		} catch (IOException | InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally
		{
			System.out.println("turn off servos");
			leftServo.setOutput(0);
			rightServo.setOutput(0);
		}
	}

	private static void gyroTest()
	{
		try
		{
			new GyroProvider(1, 0x69);
			Thread.sleep(1000 * 60 * 5);
		} catch (IOException | InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void adafruitpwm()
	{
		try
		{

			Adafruit16PwmProvider provider = setupPwm();

			provider.export(Adafruit16PwmPin.GPIO_00, PinMode.PWM_OUTPUT);

			for (int i = 0; i < 4096; i += 10)
			{
				for (int j = 0; j < 15; j++)
					provider.setPwm(j, 0, i);
				System.out.println(i);
				Thread.sleep(100);
			}
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void serialCode()
	{

		// create an instance of the serial communications class
		final Serial serial = SerialFactory.createInstance();

		// create and register the serial data listener
		serial.addListener(new SerialDataListener()
		{
			@Override
			public void dataReceived(SerialDataEvent event)
			{

				System.out.println(event.getData());
			}
		});
		// open the default serial port provided on the GPIO header
		serial.open(Serial.DEFAULT_COM_PORT, 9600);

	}

	private void FoldcodeTlc5940()
	{

		System.out.println("<--Pi4J--> GPIO Control Example ... started.");

		// create gpio controller
		final GpioController gpio = GpioFactory.getInstance();

		System.out.println("Configure primary gpio out");
		// start pwm on (GPIO_01 / GPIO_18) ...I'm confused - pin 12 of the
		// header // 50%

		// create soft-pwm pins (min=0 ; max=100)
		// SoftPwm.softPwmCreate(1, 2, 4);
		// GpioPinPwmOutput pwmPin =
		gpio.provisionPwmOutputPin(RaspiPin.GPIO_01, 512);

		// PWM15 - pin to listen for the pwm counter to over flow - pin 12 on
		// the header
		GpioPinDigitalInput counterOverflowPin = gpio.provisionDigitalInputPin(
				RaspiPin.GPIO_04, PinPullResistance.PULL_DOWN);

		// BLANK - pin to reset the pwm counter - actuall pin on the header is
		// 18
		final GpioPinDigitalOutput resetCounterPin = gpio
				.provisionDigitalOutputPin(RaspiPin.GPIO_05, PinState.LOW);

		Thread t = new Thread(new Runnable()
		{

			public void run()
			{
				while (true)
				{
					try
					{
						resetCounterPin.high();
						// Thread.sleep(2);
						resetCounterPin.low();
						Thread.sleep(20);
					} catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		t.setDaemon(true);
		t.start();

		// // create a gpio control trigger on the input pin ; when the input
		// goes
		// // HIGH, also set gpio pin #04 to HIGH
		// counterOverflowPin.addListener(new GpioPinListenerDigital()
		// {
		//
		// int pulseCount = 0;
		//
		// public void
		// handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent
		// event)
		// {
		//
		// // System.out.println("State change " + event.getState());
		// // pin is set to pull up, pwm sinks current holding pin LOW. pin
		// // goes high when the pwm stops sinking current
		// if (event.getState() == PinState.LOW)
		// {
		// resetCounterPin.high();
		// if (pulseCount % 25 == 0)
		// {
		// System.out.println("pulse : " + pulseCount);
		// }
		// pulseCount++;
		//
		// pulseCount %= 10000;
		//
		// } else
		// {
		// try
		// {
		// Thread.sleep(3);
		// } catch (InterruptedException e)
		// {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// resetCounterPin.low();
		// }
		//
		// }
		// });

		// XLAT - trigger 9540 to latch the data received. - pin 22 on the
		// header
		GpioPinDigitalOutput dataLatchPin = gpio
				.provisionDigitalOutputPin(RaspiPin.GPIO_06);

		try
		{

			System.out.println("Set up wiring spi");
			Spi.wiringPiSPISetup(1, 100000);

			byte[] buffer = null;
			Nibbler nibbler = new Nibbler();

			// set the roll over value to 100 cycles
			// nibbler.setPinPwmPercentage(0, 0);
			// nibbler.setPinPwmPercentage(1, 0);
			// nibbler.setPinPwmPercentage(2, 65); // <- this is important
			// nibbler.setPinPwmPercentage(3, 0x11);
			// nibbler.setPinPwmPercentage(4, 0x11);
			// nibbler.setPinPwmPercentage(5, 0x11);
			// nibbler.setPinPwmPercentage(6, 0);
			// nibbler.setPinPwmPercentage(7, 0);
			// nibbler.setPinPwmPercentage(8, 0);
			// nibbler.setPinPwmPercentage(9, 0);
			// nibbler.setPinPwmPercentage(10, 0);
			// nibbler.setPinPwmPercentage(11, 0);
			// nibbler.setPinPwmPercentage(12, 0);
			// nibbler.setPinPwmPercentage(13, 0);
			// nibbler.setPinPwmPercentage(14, 0);

			// nibbler.setPinPwmPercentage(15, 0x11);

			String asciiMap = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";

			/**
			 * I think we still have character encoding issues, it looks like
			 * any value beyond 7FF will intrude into the next pwm. also the
			 * last nibble (F) is affecting the next pwm. looks like I'll have
			 * to work out how to build from scratch so I can enhance the spi
			 * code.
			 */
			for (int i = 0; i < 0x7FF; i += 16)
			{
				System.out.println("Send values to tlc5940 (i=" + i + ")");
				for (int k = 1; k < 16; k++)
					nibbler.setPinPwmPercentage(k, 0x111);
				nibbler.setPinPwmPercentage(0, i);
				nibbler.setPinPwmPercentage(1, 0x7FF - i);
				// nibbler.setPinPwmPercentage(4, i);
				// nibbler.setPinPwmPercentage(5, i);
				buffer = nibbler.getBytes();

				// CharsetDecoder utf8Decoder =
				// Charset.forName("ISO-8859-1").newDecoder();
				// utf8Decoder.onMalformedInput(CodingErrorAction.REPORT);
				// utf8Decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
				// CharBuffer charBuf =
				// utf8Decoder.decode(ByteBuffer.wrap(buffer));
				// String buffer2 = charBuf.toString();
				//
				//
				System.out.println("b2 length " + buffer.length);
				for (Byte b : buffer)
				{
					System.out.print(String.format("%02X ", b));
				}
				System.out.println("");
				System.out.println(buffer);

				Spi.wiringPiSPIDataRW(1, buffer, buffer.length);
				// System.out.println("Send latch");
				dataLatchPin.high();
				dataLatchPin.low();
				Thread.sleep(10);

				// if (i == 0)
				// {
				// System.out.println("pulse reset");
				// resetCounterPin.pulse(1000, PinState.LOW);
				// Thread.sleep(1000);
				// System.out.println("pulse reset");
				// resetCounterPin.pulse(1000, PinState.HIGH);
				// resetCounterPin.pulse(1000, PinState.LOW);
				// }
			}
			System.out.println("Exiting...");

		} catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
