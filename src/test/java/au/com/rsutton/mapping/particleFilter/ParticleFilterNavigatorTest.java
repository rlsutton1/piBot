package au.com.rsutton.mapping.particleFilter;

import java.util.Random;

import au.com.rsutton.mapping.KitchenMapBuilder;
import au.com.rsutton.robot.RobotSimulator;

public class ParticleFilterNavigatorTest extends ParticleFilterNavigatorLiveTest
{

	@Override
	protected RobotSimulator getRobot()
	{

		RobotSimulator robot = new RobotSimulator(KitchenMapBuilder.buildKitchenMap());
		robot.setLocation(-150, 300, new Random().nextInt(360));
		return robot;
	}
}
