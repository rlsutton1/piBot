package au.com.rsutton.calabrate;

import java.io.IOException;

import com.pi4j.gpio.extension.grovePi.GrovePiPin;
import com.pi4j.gpio.extension.grovePi.GrovePiProvider;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import au.com.rsutton.config.Config;
import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;
import au.com.rsutton.i2c.I2cSettings;
import au.com.rsutton.robot.rover.DeadReconing;
import au.com.rsutton.robot.rover.SpeedHeadingController;
import au.com.rsutton.robot.rover.WheelController;
import au.com.rsutton.robot.rover.WheelFactory;

public class CalabrateRightWheel implements Runnable
{

	private GrovePiProvider grove;
	private WheelController rightWheel;
	private WheelController leftWheel;
	private DeadReconing reconing;
	private SpeedHeadingController speedHeadingController;

	public CalabrateRightWheel() throws IOException, InterruptedException, UnsupportedBusNumberException
	{
		Config config = new Config();

		grove = new GrovePiProvider(I2cSettings.busNumber, 4);

		grove.setMode(GrovePiPin.GPIO_A1, PinMode.ANALOG_INPUT);

		rightWheel = WheelFactory.setupRightWheel(grove, config);

		rightWheel.setSpeed(new Speed(new Distance(10, DistanceUnit.CM), Time.perSecond()));

		Thread.sleep(5000);
		rightWheel.setSpeed(new Speed(new Distance(0, DistanceUnit.CM), Time.perSecond()));

		System.out.println(rightWheel.getDistance());

		Thread.sleep(20000);
		System.exit(0);

	}

	@Override
	public void run()
	{
		try
		{

			reconing.updateLocation(rightWheel.getDistance(), leftWheel.getDistance());

			speedHeadingController.setActualHeading(reconing.getHeading());

		} catch (Exception e)
		{
		}
	}
}
