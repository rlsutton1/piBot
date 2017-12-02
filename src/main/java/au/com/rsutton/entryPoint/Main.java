package au.com.rsutton.entryPoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeUnit;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import au.com.rsutton.robot.rover.Rover;

public class Main
{
	boolean distanceOk = true;

	public static void main(String[] args)
			throws InterruptedException, IOException, BrokenBarrierException, UnsupportedBusNumberException
	{

		configureGpioForGrove();

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("Press 0 to start the rover\n");
		System.out.println("Press 1 to calabrate compass");

		System.out.println("Press 2 to calabrate dead reconning");

		System.out.println("Press 3 to calabrate right wheel");

		System.out.println("Press 4 to calabrate left wheel");

		System.out.println("Press 5 to perform straight line test");

		System.out.println("Press 6 to perform circle test");

		System.out.println("Press 7 to perform roomba test");

		// int ch = br.read();
		// if (ch == '0')
		// {
		new Rover();
		while (true)
		{
			Thread.sleep(1000);
		}
		// } else if (ch == '1')
		// {
		// new CalabrateCompass();
		// } else if (ch == '2')
		// {
		// new CalabrateDeadReconning();
		// } else if (ch == '3')
		// {
		// new CalabrateRightWheel();
		// } else if (ch == '4')
		// {
		// new CalabrateLeftWheel();
		// } else if (ch == '5')
		// {
		// new StraightLineTest();
		// } else if (ch == '6')
		// {
		// new CircleTest();
		// } else if (ch == '7')
		// {
		// new RoombaTest();
		// }

	}

	private static void configureGpioForGrove() throws InterruptedException
	{
		// the grove continuously resets if GPIO_10 is low

		final GpioController gpio = GpioFactory.getInstance();

		GpioPinDigitalOutput myLed1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_10);

		myLed1.high();

		TimeUnit.SECONDS.sleep(1);
	}

}
