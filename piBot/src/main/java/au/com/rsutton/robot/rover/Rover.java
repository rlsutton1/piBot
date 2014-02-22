package au.com.rsutton.robot.rover;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.hazelcast.SetMotion;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.pi4j.gpio.extension.adafruit.Adafruit16PwmPin;
import com.pi4j.gpio.extension.adafruit.Adafruit16PwmProvider;
import com.pi4j.gpio.extension.adafruit.PwmPin;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.RaspiPin;

public class Rover implements Runnable
{

	private WheelController leftWheel;
	private WheelController rightWheel;
	final private DeadReconing reconing;
	final private SpeedHeadingController speedHeadingController;
	final private Compass compass;
	private RobotLocation previousLocation;
	final DistanceUnit distUnit = DistanceUnit.MM;
	final TimeUnit timeUnit = TimeUnit.SECONDS;
	private long lastTime;
	final private Adafruit16PwmProvider provider;

	Rover() throws IOException, InterruptedException
	{
		provider = new Adafruit16PwmProvider(1, 0x40);
		provider.setPWMFreq(30);

		setupLeftWheel();
		setupRightWheel();
		reconing = new DeadReconing();
		compass = new Compass();
		speedHeadingController = new SpeedHeadingController(leftWheel, rightWheel);

		// listen for motion commands thru Hazelcast
		SetMotion message = new SetMotion();
		message.addMessageListener(new MessageListener<SetMotion>()
		{

			@Override
			public void onMessage(Message<SetMotion> message)
			{
				SetMotion data = message.getMessageObject();
				speedHeadingController.setDesiredMotion(data);
			}
		});
		
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this,
				100, 100, TimeUnit.MILLISECONDS);


	}

	private void setupRightWheel()
	{
		provider.export(Adafruit16PwmPin.GPIO_04, PinMode.PWM_OUTPUT);
		provider.export(Adafruit16PwmPin.GPIO_05, PinMode.PWM_OUTPUT);

		Pin rightQuadratureA = RaspiPin.GPIO_02;
		PwmPin rightDirectionPin = (PwmPin) Adafruit16PwmPin.GPIO_04;
		PwmPin rightPwmPin = (PwmPin) Adafruit16PwmPin.GPIO_05;
		Pin rightQuadreatureB = RaspiPin.GPIO_03;
		rightWheel = new WheelController(rightPwmPin, rightDirectionPin,
				rightQuadratureA, rightQuadreatureB, false, false);
	}

	private void setupLeftWheel()
	{
		provider.export(Adafruit16PwmPin.GPIO_00, PinMode.PWM_OUTPUT);
		provider.export(Adafruit16PwmPin.GPIO_01, PinMode.PWM_OUTPUT);

		Pin leftQuadratureA = RaspiPin.GPIO_05;
		PwmPin leftPwmPin = (PwmPin) Adafruit16PwmPin.GPIO_00;
		Pin leftQuadreatureB = RaspiPin.GPIO_04;
		PwmPin leftDirectionPin = (PwmPin) Adafruit16PwmPin.GPIO_01;
		leftWheel = new WheelController(leftPwmPin, leftDirectionPin,
				leftQuadratureA, leftQuadreatureB, false, false);
	}

	@Override
	public void run()
	{
		int heading = compass.getHeading();
		speedHeadingController.setActualHeading(heading);
		reconing.setHeading(heading);
		reconing.updateLocation(leftWheel.getDistance(),
				rightWheel.getDistance());

		Speed speed = calculateSpeed();


		// send location out on HazelCast
		RobotLocation currentLocation = new RobotLocation();
		currentLocation.setHeading(heading);
		currentLocation.setX(reconing.getX());
		currentLocation.setY(reconing.getY());
		currentLocation.setSpeed(speed);
		currentLocation.publish();

		previousLocation = currentLocation;

	}

	private Speed calculateSpeed()
	{
		long now = System.currentTimeMillis();

		// use pythag to calculate distance between current location and
		// previous location
		double distance = Math.sqrt(Math.pow(reconing.getX().convert(distUnit)
				- previousLocation.getX().convert(distUnit), 2)
				+ (Math.pow(reconing.getY().convert(distUnit)
						- previousLocation.getY().convert(distUnit), 2)));

		// scale up distance to a per second rate
		distance = distance * (1000.0d / ((double) (now - lastTime)));

		Speed speed = new Speed(new Distance(distance, distUnit),
				Time.perSecond());
		lastTime = now;
		return speed;
	}

}
