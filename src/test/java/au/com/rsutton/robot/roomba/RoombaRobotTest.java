package au.com.rsutton.robot.roomba;

import static org.junit.Assert.fail;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;

public class RoombaRobotTest
{

	@Test
	public void test()
	{
		RoombaRobot robot = new RoombaRobot();

		for (int y = -10; y < 60; y += 2)
		{
			for (int x = -30; x < 30; x++)
			{
				Vector3D vector = new Vector3D(x, y, 0);
				if (robot.isObsticleNear(vector))
				{
					System.out.print("1");
				} else
				{
					System.out.print("0");
				}
			}
			System.out.println("");
		}
		fail("Not yet implemented");
	}

}
