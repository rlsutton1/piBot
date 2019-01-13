package au.com.rsutton.entryPoint;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import au.com.rsutton.config.Config;
import au.com.rsutton.hazelcast.SetMotion;
import au.com.rsutton.mapping.particleFilter.DataWindow;
import au.com.rsutton.mapping.particleFilter.MapBuilder;
import au.com.rsutton.robot.roomba.RoombaRobot;
import au.com.rsutton.ui.VideoWindow;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;
import au.com.rsutton.units.Speed;
import au.com.rsutton.units.Time;

public class Main
{
	boolean distanceOk = true;

	public static void main(String[] args) throws Exception
	{

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("Press 0 to start the rover\n");
		System.out.println("Press 1 test right turn");

		System.out.println("Press 2 test left turn");

		System.out.println("Press 3 test straight");

		System.out.println("Press 4 turn on the spot");

		System.out.println("Press 5 to video test");

		System.out.println("Press 6 to perform circle test");

		System.out.println("Press 7 to perform roomba test");

		System.out.println("Press 8 to launch Map Builder UI");

		System.out.println("Press 9 to start the rover with Depth Camera Support");

		int ch = br.read();
		// if (ch == '0')
		// {

		if (ch == '0')
		{
			Config config = new Config();
			RoombaRobot robot = new RoombaRobot();
			robot.configure(config);
		}
		if (ch == '9')
		{
			Config config = new Config();
			RoombaRobot robot = new RoombaRobot();
			robot.startDepthCamera();
			robot.configure(config);
		}
		if (ch == '1')
		{
			Config config = new Config();
			RoombaRobot robot = new RoombaRobot();
			robot.configure(config);

			SetMotion setMotion = new SetMotion();
			setMotion.setSpeed(new Speed(new Distance(10, DistanceUnit.CM), Time.perSecond()));
			setMotion.setChangeHeading(-90d);
			setMotion.publish();

			TimeUnit.SECONDS.sleep(3);
			setMotion.setFreeze(true);
			setMotion.publish();
			TimeUnit.SECONDS.sleep(1);
			robot.shutdown();
			return;
		}
		if (ch == '2')
		{
			Config config = new Config();
			RoombaRobot robot = new RoombaRobot();
			robot.configure(config);

			SetMotion setMotion = new SetMotion();
			setMotion.setSpeed(new Speed(new Distance(10, DistanceUnit.CM), Time.perSecond()));
			setMotion.setChangeHeading(90d);
			setMotion.publish();

			TimeUnit.SECONDS.sleep(3);
			setMotion.setFreeze(true);
			setMotion.publish();
			TimeUnit.SECONDS.sleep(1);
			robot.shutdown();
			return;
		}
		if (ch == '3')
		{
			Config config = new Config();
			RoombaRobot robot = new RoombaRobot();
			robot.configure(config);

			SetMotion setMotion = new SetMotion();
			setMotion.setSpeed(new Speed(new Distance(10, DistanceUnit.CM), Time.perSecond()));
			setMotion.setChangeHeading(0d);
			setMotion.publish();

			TimeUnit.SECONDS.sleep(3);
			setMotion.setFreeze(true);
			setMotion.publish();
			TimeUnit.SECONDS.sleep(1);
			robot.shutdown();
			return;
		}
		if (ch == '4')
		{
			Config config = new Config();
			RoombaRobot robot = new RoombaRobot();
			robot.configure(config);

			for (int i = 0; i < 60; i++)
			{
				SetMotion setMotion = new SetMotion();
				setMotion.setSpeed(new Speed(new Distance(5, DistanceUnit.CM), Time.perSecond()));
				setMotion.setChangeHeading(150d);
				setMotion.publish();

				TimeUnit.SECONDS.sleep(1);
			}
			SetMotion setMotion = new SetMotion();
			setMotion.setFreeze(true);
			setMotion.publish();
			TimeUnit.SECONDS.sleep(1);
			robot.shutdown();
			return;
		}
		if (ch == '5')
		{
			new VideoWindow("Video Test", 0, 0);
			new DataWindow();
		}
		if (ch == '8')
		{
			new MapBuilder().test();

		}

		while (true)
		{
			Thread.sleep(1000);
		}
		// } else if (ch == '1')
		// {
		// new CalabrateCompass();
		// } else if (ch == '2')
		// {
		// new CalabrateDeadReconning();
		// } else if (ch == '3')
		// {
		// new CalabrateRightWheel();
		// } else if (ch == '4')
		// {
		// new CalabrateLeftWheel();
		// } else if (ch == '5')
		// {
		// new StraightLineTest();
		// } else if (ch == '6')
		// {
		// new CircleTest();
		// } else if (ch == '7')
		// {
		// new RoombaTest();
		// }

	}

}
