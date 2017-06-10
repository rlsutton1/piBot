package au.com.rsutton.navigation.graphslam.v3;

import static org.junit.Assert.fail;

import org.junit.Test;

public class GraphSlamV3Test2
{

	@Test
	public void test()
	{
		GraphSlamV3<GraphSlamNodeLinear> slam = new GraphSlamV3<>(getCtor());

		GraphSlamNodeLinear p1 = slam.move("p1", 20, 1);

		GraphSlamNodeLinear p2 = slam.move("p2", 20, 1);

		GraphSlamNodeLinear node0 = slam.addNode("zero", 0, 1);

		GraphSlamNodeLinear node1 = slam.addNode("one", 10, 1);

		slam.move("p3", 10, 1);

		// slam.deleteNodeRetainConstraints(p2);

		// slam.deleteNode(node0);

		slam.addConstraint(7, node0, 1);

		slam.solve();

		fail("Not yet implemented");
	}

	@Test
	public void test3()
	{
		GraphSlamV3<GraphSlamNodeLinear> slam = new GraphSlamV3<>(getCtor());

		for (int i = 0; i < 400; i++)
		{
			slam.move("p" + i, 01, 1);

			slam.addNode("f" + i, 04, 1);
			slam.solve();
		}

		slam.dump();

	}

	private GraphSlamNodeConstructor<GraphSlamNodeLinear> getCtor()
	{
		return new GraphSlamNodeConstructor<GraphSlamNodeLinear>()
		{

			@Override
			public GraphSlamNodeLinear construct(String name, double initialPosition)
			{
				return new GraphSlamNodeLinear(name, initialPosition);
			}
		};
	}

}
