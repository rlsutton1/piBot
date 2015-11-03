package au.com.rsutton.robot.rover;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.config.Config;
import au.com.rsutton.entryPoint.sonar.Sonar;
import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.entryPoint.units.Time;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.hazelcast.SetMotion;
import au.com.rsutton.i2c.I2cSettings;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.pi4j.gpio.extension.adafruit.AdafruitPCA9685;
import com.pi4j.gpio.extension.grovePi.GrovePiPin;
import com.pi4j.gpio.extension.grovePi.GrovePiProvider;
import com.pi4j.gpio.extension.lidar.Lidar;
import com.pi4j.gpio.extension.lidar.LidarDataListener;
import com.pi4j.gpio.extension.lsm303.CompassLSM303;
import com.pi4j.gpio.extension.pixy.PixyLaserRangeService;
import com.pi4j.io.gpio.PinMode;

public class Rover implements Runnable, RobotLocationReporter,
		LidarDataListener
{

	private WheelController rightWheel;
	private WheelController leftWheel;
	final private DeadReconing reconing;
	final private SpeedHeadingController speedHeadingController;
	private RobotLocation previousLocation;
	final DistanceUnit distUnit = DistanceUnit.MM;
	final TimeUnit timeUnit = TimeUnit.SECONDS;
	private long lastTime;
	// final private Adafruit16PwmProvider provider;
	private CompassLSM303 compass;
	// final private ADS1115GpioProvider ads;
	final private Sonar forwardSonar;

	private SetMotion lastData;

	volatile private Distance clearSpaceAhead = new Distance(0, DistanceUnit.MM);
	protected Distance clearSpaceLeft;
	protected Distance clearSpaceRight;

	private GrovePiProvider grove;

	public Rover() throws IOException, InterruptedException
	{

		Config config = new Config();
		
		grove = new GrovePiProvider(I2cSettings.busNumber, 4);

		grove.setMode(GrovePiPin.GPIO_A1, PinMode.ANALOG_INPUT);

		compass = new CompassLSM303(config);

		rightWheel = WheelFactory.setupRightWheel(grove,config);

		leftWheel = WheelFactory.setupLeftWheel(grove,config);

		compass.startCalabration();
		rightWheel.setSpeed(new Speed(new Distance(100, DistanceUnit.CM), Time
				.perSecond()));
		leftWheel.setSpeed(new Speed(new Distance(-100, DistanceUnit.CM), Time
				.perSecond()));
		Thread.sleep(15000);

		compass.finishcalabration();
		rightWheel.setSpeed(new Speed(new Distance(0, DistanceUnit.CM), Time
				.perSecond()));
		leftWheel.setSpeed(new Speed(new Distance(0, DistanceUnit.CM), Time
				.perSecond()));

		// provider = new Adafruit16PwmProvider(I2cSettings.busNumber, 0x40);
		// provider.setPWMFreq(30);

		// LaserControllerIfc laserController = new LaserController(provider);

		// ads = new ADS1115GpioProvider(I2cSettings.busNumber, 0x48);
		// ads.setProgrammableGainAmplifier(
		// ProgrammableGainAmplifierValue.PGA_4_096V, ADS1115Pin.INPUT_A0);
		// ads.setProgrammableGainAmplifier(
		// ProgrammableGainAmplifierValue.PGA_4_096V, ADS1115Pin.INPUT_A1);
		// ads.setProgrammableGainAmplifier(
		// ProgrammableGainAmplifierValue.PGA_4_096V, ADS1115Pin.INPUT_A2);
		forwardSonar = new Sonar(0.1, -340);
		// rightSonar = new SharpIR(1, 1800);

		// pixy = new PixyLaserRangeService(new int[] {
		// 0, 0, 0 });

		Angle initialAngle = new Angle(compass.getHeading(), AngleUnits.DEGREES);
		reconing = new DeadReconing(initialAngle);
		previousLocation = new RobotLocation();
		previousLocation.setHeading(initialAngle);
		previousLocation.setX(reconing.getX());
		previousLocation.setY(reconing.getY());
		previousLocation.setSpeed(new Speed(new Distance(0, DistanceUnit.MM),
				Time.perSecond()));

		AdafruitPCA9685 pwm = new AdafruitPCA9685(); // 0x40 is the default
		// address
		pwm.setPWMFreq(60); // Set frequency to 60 Hz

		new Lidar(pwm, this,config);

		getSpaceAhead();
		// pixy.getCurrentData();

		speedHeadingController = new SpeedHeadingController(rightWheel,
				leftWheel, compass.getHeading());

		// listen for motion commands thru Hazelcast
		SetMotion message = new SetMotion();
		message.addMessageListener(new MessageListener<SetMotion>()
		{

			@Override
			public void onMessage(Message<SetMotion> message)
			{
				SetMotion data = message.getMessageObject();
				if (clearSpaceAhead.convert(DistanceUnit.CM) < 20)
				{
					data.setSpeed(new Speed(new Distance(0, DistanceUnit.MM),
							Time.perSecond()));
				}

				// System.out.println("Setting speed" + data.getSpeed());
				speedHeadingController.setDesiredMotion(data);
				lastData = data;
			}
		});

		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this,
				200, 200, TimeUnit.MILLISECONDS);

	}

	DataValueSmoother fs = new DataValueSmoother(0.90d);

	Map<Integer, Integer> distVal = new HashMap<Integer, Integer>();
	int lc = 0;

	void getSpaceAhead() throws IOException
	{
		double value = grove.getValue(GrovePiPin.GPIO_A1);
		// double value = ads.getValue(ADS1115Pin.INPUT_A0);
		// System.out.println("Raw csa "+value);
		clearSpaceAhead = forwardSonar.getCurrentDistance((int) value);
		clearSpaceAhead = new Distance(40, DistanceUnit.CM);
		if (lastData != null
				&& lastData.getSpeed().getSpeed(distUnit, timeUnit) > 0
				&& clearSpaceAhead.convert(DistanceUnit.CM) < 30)
		{
			lastData.setSpeed(new Speed(new Distance(0, DistanceUnit.MM), Time
					.perSecond()));
			speedHeadingController.setDesiredMotion(lastData);
		}

	}

	@Override
	public void run()
	{
		try
		{

			// System.out.println("run Rover");
			getSpaceAhead();

			reconing.updateLocation(rightWheel.getDistance(), leftWheel
					.getDistance(), new Angle(compass.getHeading(),
					AngleUnits.DEGREES));

			speedHeadingController.setActualHeading((int) reconing.getHeading()
					.getDegrees());

			Speed speed = calculateSpeed();

			// send location out on HazelCast
			RobotLocation currentLocation = new RobotLocation();
			currentLocation.setHeading(reconing.getHeading());
			currentLocation.setX(reconing.getX());
			currentLocation.setY(reconing.getY());
			currentLocation.setSpeed(speed);
			currentLocation.setClearSpaceAhead(clearSpaceAhead);

			Set<LidarObservation> observations = new HashSet<>();
			observations.addAll(currentObservations);
			currentObservations.clear();
			currentLocation.addObservations(observations);

			currentLocation.publish();

			previousLocation = currentLocation;
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
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

	Set<LidarObservation> currentObservations = Collections
			.newSetFromMap(new ConcurrentHashMap<LidarObservation, Boolean>());

	@Override
	public void addLidarData(Vector3D vector, double distanceCm,
			double angleDegrees)
	{
		System.out.println((int) vector.getX() + " " + (int) vector.getY()+" "+distanceCm);

		currentObservations.add(new LidarObservation(vector, distanceCm,
				angleDegrees));

	}

}
