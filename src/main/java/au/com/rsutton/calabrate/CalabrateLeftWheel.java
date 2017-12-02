package au.com.rsutton.calabrate;

import java.io.IOException;

import com.pi4j.gpio.extension.grovePi.GrovePiPin;
import com.pi4j.gpio.extension.grovePi.GrovePiProvider;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import au.com.rsutton.config.Config;
import au.com.rsutton.i2c.I2cSettings;
import au.com.rsutton.robot.rover.DeadReconing;
import au.com.rsutton.robot.rover.SpeedHeadingController;
import au.com.rsutton.robot.rover.WheelController;
import au.com.rsutton.robot.rover5.Rover5SingleWheelControllerImpl;
import au.com.rsutton.robot.rover5.WheelControllerRover5;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;
import au.com.rsutton.units.Speed;
import au.com.rsutton.units.Time;

public class CalabrateLeftWheel implements Runnable
{

	private GrovePiProvider grove;
	private WheelController wheels;
	private Rover5SingleWheelControllerImpl leftWheel;
	private DeadReconing reconing;
	private SpeedHeadingController speedHeadingController;

	public CalabrateLeftWheel() throws IOException, InterruptedException, UnsupportedBusNumberException
	{
		Config config = new Config();

		grove = new GrovePiProvider(I2cSettings.busNumber, 4);

		grove.setMode(GrovePiPin.GPIO_A1, PinMode.ANALOG_INPUT);

		wheels = new WheelControllerRover5(grove, config);

		wheels.setSpeed(new Speed(new Distance(10, DistanceUnit.CM), Time.perSecond()), Speed.ZERO);

		Thread.sleep(5000);
		wheels.setSpeed(Speed.ZERO, Speed.ZERO);

		System.out.println(wheels.getDistanceLeftWheel());

		Thread.sleep(20000);
		System.exit(0);

	}

	@Override
	public void run()
	{
		try
		{

			reconing.updateLocation(wheels);

			speedHeadingController.setActualHeading(reconing.getHeading());

		} catch (Exception e)
		{
		}
	}
}
