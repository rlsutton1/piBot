package com.pi4j.gpio.extension.grovePi;

import java.io.IOException;

import com.pi4j.gpio.extension.lsm303.CompassLSM303;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import au.com.rsutton.config.Config;
import au.com.rsutton.robot.roomba.DifferentialDriveController;
import au.com.rsutton.robot.rover5.WheelControllerRover5;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;
import au.com.rsutton.units.Speed;
import au.com.rsutton.units.Time;

public class GrovePiTest
{

	public static void main(String[] args) throws IOException, InterruptedException, UnsupportedBusNumberException
	{

		compassTest();

		// singleTest();
		// singleWheelTest();

	}

	private static void compassTest() throws IOException, InterruptedException, UnsupportedBusNumberException
	{
		Config config = new Config();

		CompassLSM303 compass = new CompassLSM303(config);

		for (int i = 0; i < 100; i++)
		{
			System.out.println("heading " + compass.getHeadingData());
			Thread.sleep(1000);
		}

	}

	private static void singleWheelTest() throws IOException, InterruptedException, UnsupportedBusNumberException
	{
		Config config = new Config();

		GrovePiProvider grovePiProvider = new GrovePiProvider(1, 04);

		// WheelController left = setupLeftWheel(grovePiProvider);
		DifferentialDriveController wheels = new WheelControllerRover5(grovePiProvider, config);

		for (int i = -100; i < 100; i += 1)
		{
			// left.setSpeed(new Speed(new Distance(i, DistanceUnit.MM), Time
			// .perSecond()));
			wheels.setSpeed(Speed.ZERO, new Speed(new Distance(i, DistanceUnit.MM), Time.perSecond()));
			Thread.sleep(1000);
			System.out.println("Setting speed -> " + i);
		}
		// left.setSpeed(new Speed(new Distance(0, DistanceUnit.MM), Time
		// .perSecond()));

		wheels.setSpeed(Speed.ZERO, Speed.ZERO);
	}

	public static void singleTest() throws IOException, InterruptedException, UnsupportedBusNumberException
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
