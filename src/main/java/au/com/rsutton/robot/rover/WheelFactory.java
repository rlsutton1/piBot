package au.com.rsutton.robot.rover;

import java.io.IOException;

import com.pi4j.gpio.extension.adafruit.DigitalOutPin;
import com.pi4j.gpio.extension.adafruit.PwmPin;
import com.pi4j.gpio.extension.grovePi.GrovePiPin;
import com.pi4j.gpio.extension.grovePi.GrovePiProvider;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

import au.com.rsutton.config.Config;

public class WheelFactory
{
	static public WheelController setupRightWheel(GrovePiProvider grove, Config config)
			throws IOException
	{
		PwmPin pwmPin = new PwmPin(grove, GrovePiPin.GPIO_D3);
		DigitalOutPin directionPin = new DigitalOutPin(grove,
				GrovePiPin.GPIO_D4);
		Pin quadratureA = RaspiPin.GPIO_05;
		Pin quadreatureB = RaspiPin.GPIO_04;
		return new WheelController(pwmPin, directionPin, quadratureA,
				quadreatureB, false, false, 0.3,config,"right");
	}

	static public WheelController setupLeftWheel(GrovePiProvider grove, Config config)
			throws IOException
	{
		PwmPin pwmPin = new PwmPin(grove, GrovePiPin.GPIO_D6);
		DigitalOutPin directionPin = new DigitalOutPin(grove,
				GrovePiPin.GPIO_D7);
		Pin quadratureA = RaspiPin.GPIO_02;
		Pin quadreatureB = RaspiPin.GPIO_03;
		return new WheelController(pwmPin, directionPin, quadratureA,
				quadreatureB, false, true, 0.3,config,"left");
	}

}
