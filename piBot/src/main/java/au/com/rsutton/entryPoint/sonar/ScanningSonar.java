package au.com.rsutton.entryPoint.sonar;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import au.com.rsutton.entryPoint.controllers.ServoController;
import au.com.rsutton.i2c.I2cSettings;

import com.pi4j.gpio.extension.adafruit.ADS1115;
import com.pi4j.gpio.extension.adafruit.Adafruit16PwmPin;
import com.pi4j.gpio.extension.adafruit.Adafruit16PwmProvider;
import com.pi4j.gpio.extension.adafruit.PwmPin;
import com.pi4j.io.gpio.PinMode;

public class ScanningSonar implements Runnable
{

	private ServoController controller1;

	int[] pos = new int[] {
			-90, -45, 0, 45, 90, 45, 0, -45 };
	int idx = 0;

	private Sonar sonar;


	private ScanningSonarListener listener;

	public ScanningSonar(Adafruit16PwmProvider provider,
			ScanningSonarListener listener) throws IOException
	{


		ADS1115 ads = new ADS1115(I2cSettings.busNumber, 0x48);

//		sonar = new Sonar(0.1, 2880, 0);
//		ads.addListener(sonar);

		provider.export(Adafruit16PwmPin.GPIO_08, PinMode.PWM_OUTPUT);

		PwmPin servoPin1 = new PwmPin(provider, Adafruit16PwmPin.GPIO_08);

		controller1 = new ServoController(servoPin1, 81, 307, 1);

		this.listener = listener;

		System.out.println("Starting Scanning sonar");

		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this,
				300, 300, TimeUnit.MILLISECONDS);

	}


	@Override
	public void run()
	{

		try
		{
//			int p = pos[idx];
//			RangeData data = new RangeData(p,
//					sonar.getCurrentDistance(), new Time(
//							System.currentTimeMillis(), TimeUnit.MILLISECONDS));
//			System.out.println(data);
//			listener.notifiyDistance(data);
//
//			controller1.setOutput(p);
//			idx++;
//			idx = idx % pos.length;
		} catch (Throwable e)
		{
			e.printStackTrace();
		}

	}
}
