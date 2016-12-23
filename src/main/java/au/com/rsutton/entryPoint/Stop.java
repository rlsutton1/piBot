package au.com.rsutton.entryPoint;

import java.io.IOException;

import com.pi4j.gpio.extension.adafruit.Adafruit16PwmPin;
import com.pi4j.gpio.extension.adafruit.Adafruit16PwmProvider;
import com.pi4j.gpio.extension.adafruit.PwmPin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import au.com.rsutton.entryPoint.controllers.ServoController;
import au.com.rsutton.i2c.I2cSettings;

public class Stop
{

	/**
	 * @param args
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws UnsupportedBusNumberException
	 */
	public static void main(String[] args) throws InterruptedException, IOException, UnsupportedBusNumberException
	{
		ServoController leftServo = null;
		ServoController rightServo = null;

		try
		{

			Adafruit16PwmProvider provider = new Adafruit16PwmProvider(I2cSettings.busNumber, 0x40);

			provider.setPWMFreq(30);

			provider.export(Adafruit16PwmPin.GPIO_00, PinMode.PWM_OUTPUT);
			provider.export(Adafruit16PwmPin.GPIO_01, PinMode.PWM_OUTPUT);

			PwmPin leftServoPin = new PwmPin(provider, Adafruit16PwmPin.GPIO_00);
			PwmPin rightServoPin = new PwmPin(provider, Adafruit16PwmPin.GPIO_01);

			leftServo = new ServoController(leftServoPin, 2048, 4095, ServoController.NORMAL);
			rightServo = new ServoController(rightServoPin, 2048, 4095, ServoController.NORMAL);

			leftServo.turnOff();
			rightServo.turnOff();

		} catch (IOException | InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally
		{
			System.out.println("turn off servos");
			leftServo.turnOff();
			rightServo.turnOff();
		}

		// gyroCalabrate();
	}

}
