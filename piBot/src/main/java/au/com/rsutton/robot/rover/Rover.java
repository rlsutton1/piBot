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
import com.pi4j.gpio.extension.lsm303.CompassLSM303;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.RaspiPin;

public class Rover implements Runnable
{

	private WheelController rightWheel;
	private WheelController leftWheel;
	final private DeadReconing reconing;
	final private SpeedHeadingController speedHeadingController;
	private RobotLocation previousLocation;
	final DistanceUnit distUnit = DistanceUnit.MM;
	final TimeUnit timeUnit = TimeUnit.SECONDS;
	private long lastTime;
	final private Adafruit16PwmProvider provider;
	private CompassLSM303 compass;

	public Rover() throws IOException, InterruptedException
	{
		compass = new CompassLSM303();
		compass.setup();

		provider = new Adafruit16PwmProvider(1, 0x40);
		provider.setPWMFreq(30);

		setupRightWheel();

		setupLeftWheel();
		
		 


		reconing = new DeadReconing();
		 previousLocation = new RobotLocation();
		 previousLocation.setHeading(0);
		 previousLocation.setX(reconing.getX());
		 previousLocation.setY(reconing.getY());
		 previousLocation.setSpeed(new Speed(new Distance(0, DistanceUnit.MM), Time.perSecond()));
		
		
		
		speedHeadingController = new SpeedHeadingController(rightWheel,
				leftWheel,compass.getHeading());

		// listen for motion commands thru Hazelcast
		SetMotion message = new SetMotion();
		message.addMessageListener(new MessageListener<SetMotion>()
		{

			@Override
			public void onMessage(Message<SetMotion> message)
			{
				SetMotion data = message.getMessageObject();
			//	System.out.println("Setting speed" + data.getSpeed());
				speedHeadingController.setDesiredMotion(data);
			}
		});

		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this,
				100, 100, TimeUnit.MILLISECONDS);

	}

	private void setupLeftWheel()
	{
		provider.export(Adafruit16PwmPin.GPIO_04, PinMode.PWM_OUTPUT);
		provider.export(Adafruit16PwmPin.GPIO_05, PinMode.PWM_OUTPUT);

		Pin quadratureA = RaspiPin.GPIO_02;
		PwmPin directionPin = new PwmPin(provider, Adafruit16PwmPin.GPIO_05);
		PwmPin pwmPin = new PwmPin(provider, Adafruit16PwmPin.GPIO_04);
		Pin quadreatureB = RaspiPin.GPIO_03;
		leftWheel = new WheelController(pwmPin, directionPin, quadratureA,
				quadreatureB, false, true);
	}

	private void setupRightWheel()
	{
		provider.export(Adafruit16PwmPin.GPIO_00, PinMode.PWM_OUTPUT);
		provider.export(Adafruit16PwmPin.GPIO_01, PinMode.PWM_OUTPUT);

		Pin quadratureA = RaspiPin.GPIO_05;
		PwmPin pwmPin = new PwmPin(provider, Adafruit16PwmPin.GPIO_01);

		Pin quadreatureB = RaspiPin.GPIO_04;
		PwmPin directionPin = new PwmPin(provider, Adafruit16PwmPin.GPIO_00);
		rightWheel = new WheelController(pwmPin, directionPin, quadratureA,
				quadreatureB, false, false);
	}

	@Override
	public void run()
	{
		try
		{
			
			int heading = (int) compass.getHeading();
			speedHeadingController.setActualHeading(heading);
			reconing.setHeading(heading);
			reconing.updateLocation(rightWheel.getDistance(),
					leftWheel.getDistance());

			Speed speed = calculateSpeed();

			// send location out on HazelCast
			RobotLocation currentLocation = new RobotLocation();
			currentLocation.setHeading(heading);
			currentLocation.setX(reconing.getX());
			currentLocation.setY(reconing.getY());
			currentLocation.setSpeed(speed);
			currentLocation.publish();

			previousLocation = currentLocation;
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
