package com.pi4j.gpio.extension.grovePi;

import java.io.IOException;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;
import au.com.rsutton.robot.rover.WheelController;
import au.com.rsutton.robot.rover.WheelFactory;

import com.pi4j.gpio.extension.lsm303.CompassLSM303;

public class GrovePiTest
{

	public static void main(String[] args) throws IOException,
			InterruptedException
	{

		compassTest();
			
//		singleTest();
//		singleWheelTest();

	}

	private static void compassTest() throws IOException, InterruptedException
	{
		CompassLSM303 compass = new CompassLSM303();

		for (int i = 0; i < 100; i++)
		{
			System.out.println("heading " + compass.getHeading());
			Thread.sleep(1000);
		}

	}

	private static void singleWheelTest() throws IOException,
			InterruptedException
	{
		GrovePiProvider grovePiProvider = new GrovePiProvider(1, 04);

		// WheelController left = setupLeftWheel(grovePiProvider);
		WheelController right = WheelFactory.setupRightWheel(grovePiProvider);

		for (int i = -100; i < 100; i += 1)
		{
			// left.setSpeed(new Speed(new Distance(i, DistanceUnit.MM), Time
			// .perSecond()));
			right.setSpeed(new Speed(new Distance(i, DistanceUnit.MM), Time
					.perSecond()));
			Thread.sleep(1000);
			System.out.println("Setting speed -> " + i);
		}
		// left.setSpeed(new Speed(new Distance(0, DistanceUnit.MM), Time
		// .perSecond()));

		right.setSpeed(new Speed(new Distance(0, DistanceUnit.MM), Time
				.perSecond()));
	}

	public static void singleTest() throws IOException, InterruptedException
	{

		GrovePiProvider grovePiProvider = new GrovePiProvider(1, 04);
		for (int i = -255; i < 255; i += 10)
		{
			int direction = 1;
			if (i < 0)
			{
				direction = 0;
			}
			grovePiProvider.setPwm(GrovePiPin.GPIO_D3, Math.abs(i));
			// grovePiProvider.setPwm(GrovePiPin.GPIO_D6, Math.abs(i));

			// grovePiProvider.setValue(GrovePiPin.GPIO_D7, direction);
			grovePiProvider.setValue(GrovePiPin.GPIO_D4, direction);
			Thread.sleep(300);
			System.out.println(i);
		}
		grovePiProvider.setPwm(GrovePiPin.GPIO_D3, 0);
		grovePiProvider.setPwm(GrovePiPin.GPIO_D6, 0);

	}

}
