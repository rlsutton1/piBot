package au.com.rsutton.calabrate;

import java.io.IOException;

import com.pi4j.gpio.extension.grovePi.GrovePiPin;
import com.pi4j.gpio.extension.grovePi.GrovePiProvider;
import com.pi4j.gpio.extension.lsm303.CompassLSM303;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import au.com.rsutton.config.Config;
import au.com.rsutton.i2c.I2cSettings;
import au.com.rsutton.robot.rover.WheelController;
import au.com.rsutton.robot.rover5.WheelControllerRover5;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;
import au.com.rsutton.units.Speed;
import au.com.rsutton.units.Time;

public class CalabrateCompass
{

	private GrovePiProvider grove;
	private CompassLSM303 compass;
	private WheelController wheels;

	public CalabrateCompass() throws IOException, InterruptedException, UnsupportedBusNumberException
	{
		Config config = new Config();

		grove = new GrovePiProvider(I2cSettings.busNumber, 4);

		grove.setMode(GrovePiPin.GPIO_A1, PinMode.ANALOG_INPUT);

		compass = new CompassLSM303(config);

		wheels = new WheelControllerRover5(grove, config);

		compass.startCalabration();
		wheels.setSpeed(new Speed(new Distance(100, DistanceUnit.CM), Time.perSecond()),
				new Speed(new Distance(-100, DistanceUnit.CM), Time.perSecond()));
		Thread.sleep(15000);

		compass.finishcalabration();
		wheels.setSpeed(Speed.ZERO, Speed.ZERO);

		compass.saveConfig();

		config.save();

		System.exit(0);

	}
}
