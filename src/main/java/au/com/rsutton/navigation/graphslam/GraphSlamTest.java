package au.com.rsutton.navigation.graphslam;

import org.junit.Test;

public class GraphSlamTest
{

	@Test
	public void test()
	{

		System.out.println("Test move");
		GraphSlam slam = new GraphSlam(0);

		slam.move(5, 1);
		slam.dumpAllData();

	}

	@Test
	public void test2()
	{

		System.out.println("Test move and landmark 4");
		GraphSlam slam = new GraphSlam(0);

		slam.move(5, 1);
		slam.dumpAllData();
		slam.dumpPositions();
		int pos = slam.addNode(9, 1);

		slam.dumpAllData();
		slam.dumpPositions();
		slam.update(pos, 8, 1);

		slam.dumpAllData();
		slam.dumpPositions();
		slam.move(10, 1);
		slam.dumpAllData();
		slam.dumpPositions();
		pos = slam.addNode(15, 1);
		slam.update(pos, 14, 1);

		slam.dumpAllData();
		slam.dumpPositions();

		slam.dumpPositions();

		System.out.println("DONE");

	}

	@Test
	public void test1()
	{
		GraphSlam slam = new GraphSlam(0);

		int pos = slam.addNode(10, 1);
		int pos2 = slam.addNode(20, 1);
		slam.dumpAllData();

	}

	@Test
	public void test3()
	{
		GraphSlam slam = new GraphSlam(0);

		for (int i = 0; i < 200; i++)
		{
			slam.move(01, 1);

			slam.addNode(04, 1);
		}

		slam.dumpAllData();

	}

}
