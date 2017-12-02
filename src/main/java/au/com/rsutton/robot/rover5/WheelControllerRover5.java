package au.com.rsutton.robot.rover5;

import java.io.IOException;

import com.pi4j.gpio.extension.adafruit.DigitalOutPin;
import com.pi4j.gpio.extension.adafruit.PwmPin;
import com.pi4j.gpio.extension.grovePi.GrovePiPin;
import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

import au.com.rsutton.config.Config;
import au.com.rsutton.robot.rover.WheelController;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.Speed;

public class WheelControllerRover5 implements WheelController
{

	private Rover5SingleWheelControllerImpl right;
	private Rover5SingleWheelControllerImpl left;

	public WheelControllerRover5(GpioProvider grove, Config config) throws IOException
	{

		PwmPin rPwmPin = new PwmPin(grove, GrovePiPin.GPIO_D3);
		DigitalOutPin rDirectionPin = new DigitalOutPin(grove, GrovePiPin.GPIO_D4);
		Pin rQuadratureA = RaspiPin.GPIO_02;
		Pin rQuadreatureB = RaspiPin.GPIO_03;
		right = new Rover5SingleWheelControllerImpl(rPwmPin, rDirectionPin, rQuadratureA, rQuadreatureB, true, false,
				0.3, config, "right");

		PwmPin lPwmPin = new PwmPin(grove, GrovePiPin.GPIO_D6);
		DigitalOutPin lDirectionPin = new DigitalOutPin(grove, GrovePiPin.GPIO_D7);
		Pin lQuadratureA = RaspiPin.GPIO_05;
		Pin lQuadreatureB = RaspiPin.GPIO_04;
		left = new Rover5SingleWheelControllerImpl(lPwmPin, lDirectionPin, lQuadratureA, lQuadreatureB, false, false,
				0.3, config, "left");

	}

	@Override
	public void setSpeed(Speed leftSpeed, Speed rightSpeed)
	{
		left.setSpeed(leftSpeed);
		right.setSpeed(rightSpeed);

	}

	@Override
	public Distance getDistanceLeftWheel()
	{
		return left.getDistance();
	}

	@Override
	public Distance getDistanceRightWheel()
	{
		return right.getDistance();
	}

}
