package au.com.rsutton.entryPoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.BrokenBarrierException;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import au.com.rsutton.calabrate.CalabrateCompass;
import au.com.rsutton.calabrate.CalabrateDeadReconning;
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
		}
	}

}
