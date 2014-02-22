package au.com.rsutton.entryPoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import au.com.rsutton.entryPoint.controllers.HBridgeController;
import au.com.rsutton.entryPoint.controllers.ServoController;
import au.com.rsutton.entryPoint.controllers.VehicleHeadingController;
import au.com.rsutton.entryPoint.sonar.RangeDataListener;
import au.com.rsutton.entryPoint.sonar.RangeRateData;
import au.com.rsutton.entryPoint.sonar.RateOfCloseRanger;
import au.com.rsutton.entryPoint.sonar.ScanningSonar;
import au.com.rsutton.entryPoint.sonar.Sonar;
import au.com.rsutton.entryPoint.units.DistanceUnit;

import com.pi4j.gpio.extension.adafruit.ADS1115;
import com.pi4j.gpio.extension.adafruit.Adafruit16PwmPin;
import com.pi4j.gpio.extension.adafruit.Adafruit16PwmProvider;
import com.pi4j.gpio.extension.adafruit.GyroProvider;
import com.pi4j.gpio.extension.adafruit.PwmPin;
import com.pi4j.io.gpio.PinMode;

public class Controller
{
	boolean distanceOk = true;
	private int lastDistance;
	int heading = 0;
	double forwardPressure = 0;
	double latteralPressure = 0;
	int speed = 0;
	private VehicleHeadingController controller;

	public Controller() throws InterruptedException, IOException
	{

	}

	void start() throws InterruptedException, IOException
	{
		Thread.sleep(2000);
		GyroProvider gyro = new GyroProvider(1, 0x69);

		Adafruit16PwmProvider provider = setupPwm();

		controller = setupVehicleController(gyro, provider);

		controller.loadConfig();

		ADS1115 ads = new ADS1115(1, 0x48);

		Sonar sonar = new Sonar(0.1, 2880, 0);
		ads.addListener(sonar);

		new ScanningSonar(provider, new RateOfCloseRanger(
				getRangeDataListener()));

		while (true)
		{

			Thread.sleep(30000);
		}
	}

	Map<Long, RangeRateData> storedData = new HashMap<Long, RangeRateData>();

	private RangeDataListener getRangeDataListener()
	{
		return new RangeDataListener()
		{

			@Override
			public void notifyRangeData(RangeRateData rdata)
			{

				storedData.put(rdata.getAngle(), rdata);

				int agregateHeadingChange = 0;
				int agregateSpeed = 100;
				for (RangeRateData data : storedData.values())
				{
					int headingChange = (int) (processLatteralPressure(data) * Math
							.signum(data.getAngle()));
					agregateHeadingChange += headingChange;
					agregateSpeed = Math.min(agregateSpeed,
							processForwardPressure(data));

				}
				speed = agregateSpeed;
				
				heading -= agregateHeadingChange;
				System.out.println("Speed: " + speed + " heading: " + heading);
				controller.setHeading(heading);
			}

		};
	}

	/**
	 * 
	 * @param data
	 * @return an amount of heading change in degrees
	 */
	int processLatteralPressure(RangeRateData data)
	{
		double distance = data.getDistance().convert(DistanceUnit.CM);

		distance = Math.max(0, distance); // lower bound
		distance = Math.min(50, distance); // upper bound

		distance = distance*2;
		
		return (int) (((-0.1d) * distance) + (5.0d));

		
	}

	/**
	 * 
	 * @param data
	 * @return speed 0 to 100
	 */
	int processForwardPressure(RangeRateData data)
	{

		double distance = data.getDistance().convert(DistanceUnit.CM);

		distance = Math.max(25, distance); // lower bound
		distance = Math.min(100, distance); // upper bound

		return (int) (((4.0d / 3.0d) * distance) - (100.0d / 3.0d));


	}

	private VehicleHeadingController setupVehicleController(GyroProvider gyro,
			Adafruit16PwmProvider provider) throws IOException,
			InterruptedException
	{
		provider.export(Adafruit16PwmPin.GPIO_00, PinMode.PWM_OUTPUT);
		provider.export(Adafruit16PwmPin.GPIO_01, PinMode.PWM_OUTPUT);
		provider.export(Adafruit16PwmPin.GPIO_04, PinMode.PWM_OUTPUT);
		provider.export(Adafruit16PwmPin.GPIO_05, PinMode.PWM_OUTPUT);

		PwmPin leftServoPin = new PwmPin(provider, Adafruit16PwmPin.GPIO_00);
		PwmPin leftDirectionPin = new PwmPin(provider, Adafruit16PwmPin.GPIO_01);

		PwmPin rightServoPin = new PwmPin(provider, Adafruit16PwmPin.GPIO_05);
		PwmPin rightDirectionPin = new PwmPin(provider,
				Adafruit16PwmPin.GPIO_04);

		HBridgeController leftServo = new HBridgeController(leftServoPin,
				leftDirectionPin, false);
		HBridgeController rightServo = new HBridgeController(rightServoPin,
				rightDirectionPin, false);

		leftServo.setOutput(0);
		rightServo.setOutput(0);

		VehicleHeadingController controller = new VehicleHeadingController(leftServo,
				rightServo, gyro, gyro);

		controller.autoConfigure(gyro);
		return controller;
	}

	private Adafruit16PwmProvider setupPwm() throws IOException,
			InterruptedException
	{
		Adafruit16PwmProvider provider = new Adafruit16PwmProvider(1, 0x40);
		provider.setPWMFreq(30);
		return provider;
	}

}
