package au.com.rsutton.calabrate;

import java.io.IOException;

import com.pi4j.gpio.extension.grovePi.GrovePiPin;
import com.pi4j.gpio.extension.grovePi.GrovePiProvider;
import com.pi4j.gpio.extension.lsm303.CompassLSM303;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import au.com.rsutton.config.Config;
import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;
import au.com.rsutton.i2c.I2cSettings;
import au.com.rsutton.robot.rover.WheelController;
import au.com.rsutton.robot.rover.WheelFactory;

public class CalabrateCompass
{

	private GrovePiProvider grove;
	private CompassLSM303 compass;
	private WheelController rightWheel;
	private WheelController leftWheel;

	public CalabrateCompass() throws IOException, InterruptedException, UnsupportedBusNumberException
	{
		Config config = new Config();

		grove = new GrovePiProvider(I2cSettings.busNumber, 4);

		grove.setMode(GrovePiPin.GPIO_A1, PinMode.ANALOG_INPUT);

		compass = new CompassLSM303(config);

		rightWheel = WheelFactory.setupRightWheel(grove, config);

		leftWheel = WheelFactory.setupLeftWheel(grove, config);

		compass.startCalabration();
		rightWheel.setSpeed(new Speed(new Distance(100, DistanceUnit.CM), Time.perSecond()));
		leftWheel.setSpeed(new Speed(new Distance(-100, DistanceUnit.CM), Time.perSecond()));
		Thread.sleep(15000);

		compass.finishcalabration();
		rightWheel.setSpeed(new Speed(new Distance(0, DistanceUnit.CM), Time.perSecond()));
		leftWheel.setSpeed(new Speed(new Distance(0, DistanceUnit.CM), Time.perSecond()));

		compass.saveConfig();

		config.save();

		System.exit(0);

	}
}
