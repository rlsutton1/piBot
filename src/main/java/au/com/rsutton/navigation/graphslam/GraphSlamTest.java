package au.com.rsutton.navigation.graphslam;

import org.junit.Test;

public class GraphSlamTest
{

	@Test
	public void test()
	{

		System.out.println("Test move");
		GraphSlam slam = new GraphSlam(0);

		slam.addMove(5, 1);
		slam.dumpAllData();

	}

	@Test
	public void test2()
	{

		System.out.println("Test move and landmark 4");
		GraphSlam slam = new GraphSlam(0);

		slam.addMove(5, 1);
		slam.dumpAllData();
		int pos = slam.add(9, 1);

		slam.dumpAllData();
		slam.update(pos, 8, 1);

		slam.dumpAllData();
		slam.addMove(10, 1);
		slam.add(15, 1);

		slam.dumpAllData();

		slam.dumpPositions();

		System.out.println("DONE");

	}

	@Test
	public void test1()
	{
		GraphSlam slam = new GraphSlam(0);

		int pos = slam.add(10, 1);
		int pos2 = slam.add(20, 1);
		slam.dumpAllData();

	}

}
