package au.com.rsutton.entryPoint;

import java.io.IOException;

import au.com.rsutton.i2c.I2cSettings;
import au.com.rsutton.robot.rover.Rover;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;

import com.pi4j.gpio.extension.adafruit.ADS1115;
import com.pi4j.gpio.extension.adafruit.Adafruit16PwmProvider;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactoryProviderBanana;
import com.pi4j.io.i2c.I2CFactoryProviderRaspberry;

public class Main
{
	boolean distanceOk = true;

	public static void main(String[] args) throws InterruptedException,
			IOException, V4L4JException
	{
//		I2CFactory.setFactory(new I2CFactoryProviderBanana());
		new Rover();
		while (true)
		{
			Thread.sleep(1000);
		}

	}



//	private static void sonarFullScanTest() throws IOException,
//			InterruptedException
//	{
//		Adafruit16PwmProvider provider = setupPwm();
//
//		ADS1115 ads = new ADS1115(1, 0x48);
//
//		Sonar sonar = new Sonar(0.1, 2880, 0);
//		new FullScan(provider);
//
//	}

	

	private static Adafruit16PwmProvider setupPwm() throws IOException,
			InterruptedException
	{
		Adafruit16PwmProvider provider = new Adafruit16PwmProvider(I2cSettings.busNumber, 0x40);
		provider.setPWMFreq(30);
		return provider;
	}

	
	private static void sonarTest() throws IOException, InterruptedException
	{
		ADS1115 ads = new ADS1115(I2cSettings.busNumber, 0x48);

		//Sonar sonar = new Sonar(0.1, 2880, 0);
		//ads.addListener(sonar);
		for (int i = 0; i < 5000; i++)
		{

//			int values = (int) sonar.getCurrentDistance().convert(
//					DistanceUnit.CM);
//			System.out.println("value: " + values);
			Thread.sleep(100);

		}
	}

}
