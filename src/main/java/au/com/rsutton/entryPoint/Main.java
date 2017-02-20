package au.com.rsutton.entryPoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.BrokenBarrierException;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import au.com.rsutton.calabrate.CalabrateCompass;
import au.com.rsutton.calabrate.CalabrateDeadReconning;
import au.com.rsutton.calabrate.CalabrateLeftWheel;
import au.com.rsutton.calabrate.CalabrateRightWheel;
import au.com.rsutton.calabrate.CircleTest;
import au.com.rsutton.calabrate.StraightLineTest;
import au.com.rsutton.robot.rover.Rover;

public class Main
{
	boolean distanceOk = true;

	public static void main(String[] args)
			throws InterruptedException, IOException, BrokenBarrierException, UnsupportedBusNumberException
	{

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("Press 0 to start the rover\n");
		System.out.println("Press 1 to calabrate compass");

		System.out.println("Press 2 to calabrate dead reconning");

		System.out.println("Press 3 to calabrate right wheel");

		System.out.println("Press 4 to calabrate left wheel");

		System.out.println("Press 5 to perform straight line test");

		System.out.println("Press 6 to perform circle test");

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
			new CalabrateDeadReconning();
		} else if (ch == '3')
		{
			new CalabrateRightWheel();
		} else if (ch == '4')
		{
			new CalabrateLeftWheel();
		} else if (ch == '5')
		{
			new StraightLineTest();
		} else if (ch == '6')
		{
			new CircleTest();
		}
	}

}
