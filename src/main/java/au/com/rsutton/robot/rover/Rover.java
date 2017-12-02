package au.com.rsutton.robot.rover;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.pi4j.gpio.extension.adafruit.GyroProvider;
import com.pi4j.gpio.extension.grovePi.GrovePiPin;
import com.pi4j.gpio.extension.grovePi.GrovePiProvider;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import au.com.rsutton.config.Config;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.hazelcast.SetMotion;
import au.com.rsutton.i2c.I2cSettings;
import au.com.rsutton.robot.lidar.LidarObservation;
import au.com.rsutton.robot.lidar.LidarLite.Spinner;
import au.com.rsutton.robot.rover5.WheelControllerRover5;
import au.com.rsutton.robot.rover5.sonar.Sonar;
import au.com.rsutton.units.Angle;
import au.com.rsutton.units.AngleUnits;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;
import au.com.rsutton.units.Speed;
import au.com.rsutton.units.Time;

public class Rover implements Runnable, RobotLocationReporter
{

	private WheelController wheels;
	private final DeadReconing reconing;
	private final SpeedHeadingController speedHeadingController;
	private RobotLocation previousLocation;
	static final DistanceUnit distUnit = DistanceUnit.MM;
	static final TimeUnit timeUnit = TimeUnit.SECONDS;
	private long lastTime;
	// final private Adafruit16PwmProvider provider;
	// private CompassLSM303 compass;
	// final private ADS1115GpioProvider ads;
	private final Sonar forwardSonar;

	private SetMotion lastData;

	private volatile Distance clearSpaceAhead = new Distance(0, DistanceUnit.MM);

	private GrovePiProvider grove;

	Vector3D lidarTransposeOnRobotChasis = new Vector3D(0, 10, 0);

	List<LidarObservation> currentObservations = new Vector<>();

	public Rover() throws IOException, InterruptedException, BrokenBarrierException, UnsupportedBusNumberException
	{

		Config config = new Config();

		grove = new GrovePiProvider(I2cSettings.busNumber, 4);

		grove.setMode(GrovePiPin.GPIO_A1, PinMode.ANALOG_INPUT);

		// compass = new CompassLSM303(config);

		GyroProvider gyro = new GyroProvider(I2cSettings.busNumber, GyroProvider.Addr);

		wheels = new WheelControllerRover5(grove, config);

		wheels.setSpeed(new Speed(new Distance(0, DistanceUnit.CM), Time.perSecond()),
				new Speed(new Distance(0, DistanceUnit.CM), Time.perSecond()));

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

		Angle initialAngle = new Angle(0, AngleUnits.DEGREES);
		reconing = new DeadReconing(initialAngle, gyro);
		previousLocation = new RobotLocation();
		previousLocation.setDeadReaconingHeading(initialAngle);

		// AdafruitPCA9685 pwm = new AdafruitPCA9685(); // 0x40 is the default
		// // address
		// pwm.setPWMFreq(1000); // Set frequency to 60 Hz

		// StepperMotor stepper = new StepperMotor(8);

		new Spinner(200 * 8, grove, config);

		new LidarObservation().addMessageListener(new MessageListener<LidarObservation>()
		{

			@Override
			public void onMessage(Message<LidarObservation> message)
			{

				LidarObservation lidarObservation = message.getMessageObject();

				// apply transpose for the position of the Lidar on the robot's
				// Chasis
				Vector3D vector = lidarObservation.getVector();
				vector = vector.add(lidarTransposeOnRobotChasis);
				LidarObservation observation = new LidarObservation(vector, lidarObservation.isStartOfScan());

				currentObservations.add(observation);

			}
		});

		getSpaceAhead();
		// pixy.getCurrentData();

		speedHeadingController = new SpeedHeadingController(wheels, 0);

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
					data.setSpeed(new Speed(new Distance(0, DistanceUnit.MM), Time.perSecond()));
				}

				// System.out.println("Setting speed" + data.getSpeed());
				speedHeadingController.setDesiredMotion(data);
				lastData = data;
			}
		});

		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this, 200, 200, TimeUnit.MILLISECONDS);

	}

	Map<Integer, Integer> distVal = new HashMap<>();
	int lc = 0;

	void getSpaceAhead() throws IOException
	{
		double value = grove.getValue(GrovePiPin.GPIO_A1);
		// double value = ads.getValue(ADS1115Pin.INPUT_A0);
		// System.out.println("Raw csa "+value);
		clearSpaceAhead = forwardSonar.getCurrentDistance((int) value);
		clearSpaceAhead = new Distance(40, DistanceUnit.CM);
		if (lastData != null && lastData.getSpeed().getSpeed(distUnit, timeUnit) > 0
				&& clearSpaceAhead.convert(DistanceUnit.CM) < 30)
		{
			lastData.setSpeed(new Speed(new Distance(0, DistanceUnit.MM), Time.perSecond()));
			speedHeadingController.setDesiredMotion(lastData);
		}

	}

	@Override
	public void run()
	{
		try
		{

			getSpaceAhead();

			reconing.updateLocation(wheels);

			speedHeadingController.setActualHeading(reconing.getHeading());

			// send location out on HazelCast
			RobotLocation currentLocation = new RobotLocation();
			currentLocation.setDeadReaconingHeading(new Angle(reconing.getHeading().getHeading(), AngleUnits.DEGREES));
			currentLocation.setDistanceTravelled(reconing.getTotalDistanceTravelled());
			currentLocation.setClearSpaceAhead(clearSpaceAhead);

			List<LidarObservation> observations = new LinkedList<>();

			observations.addAll(currentObservations);
			currentObservations.removeAll(observations);
			if (currentObservations.size() > 0)
			{
				System.out.println("Current observations isn't empty after publish");
			}
			currentLocation.addObservations(observations);

			currentLocation.publish();

			previousLocation = currentLocation;
		} catch (Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}

	}

}
