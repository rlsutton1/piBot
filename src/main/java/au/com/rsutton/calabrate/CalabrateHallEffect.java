package au.com.rsutton.calabrate;

import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;

import com.pi4j.gpio.extension.grovePi.GrovePiPin;
import com.pi4j.gpio.extension.grovePi.GrovePiProvider;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import au.com.rsutton.entryPoint.controllers.Pid;

public class CalabrateHallEffect
{

	private static final int HALL_EFFECT_ZERO = 500;

	public CalabrateHallEffect()
			throws IOException, InterruptedException, BrokenBarrierException, UnsupportedBusNumberException
	{
		GrovePiProvider grove = new GrovePiProvider(2, 4);// I2cSettings.busNumber,
															// 4);
		grove.setMode(GrovePiPin.GPIO_A1, PinMode.ANALOG_INPUT);

		grove.setMode(GrovePiPin.GPIO_D3, PinMode.PWM_OUTPUT);

		Pid pid = new Pid(10, .2, .1, 5, 255, 0, true);
		double target = 180;// Math.abs(HALL_EFFECT_ZERO -
							// grove.getValue(GrovePiPin.GPIO_A1));
		long previous = System.currentTimeMillis();
		int maxCtr = 0;
		for (int i = 0; i < 10000; i++)
		{
			double actual = Math.abs(HALL_EFFECT_ZERO - grove.getValue(GrovePiPin.GPIO_A1));

			int pwm = (int) pid.computePid(target, actual);
			if (pwm > 254)
			{
				maxCtr++;
				if (maxCtr > 1000)
				{
					grove.setPwm(GrovePiPin.GPIO_D3, 0);
					System.out.println("Max pwm, shutting down");
					System.exit(0);
				}
			} else
			{
				maxCtr = 0;
			}

			grove.setPwm(GrovePiPin.GPIO_D3, pwm);

			if (i % 30 == 0)
			{
				System.out.println(
						((System.currentTimeMillis() - previous) / 30) + " " + target + " " + actual + " " + pwm);
				previous = System.currentTimeMillis();
			}

		}
		grove.setPwm(GrovePiPin.GPIO_D3, 0);
		System.exit(0);

	}

}
