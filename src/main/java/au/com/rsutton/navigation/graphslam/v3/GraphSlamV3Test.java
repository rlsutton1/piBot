package au.com.rsutton.navigation.graphslam.v3;

import static org.junit.Assert.fail;

import org.junit.Test;

public class GraphSlamV3Test
{

	@Test
	public void test()
	{
		GraphSlamV3<GraphSlamNodeLinear> slam = new GraphSlamV3<>(getCtor());

		slam.dump();

		GraphSlamNodeLinear node1 = slam.addNode("one", 10, 1, slam.getRoot());

		slam.dump();

		GraphSlamNodeLinear node2 = slam.addNode("two", 3, 1, slam.getRoot());

		slam.dump();

		slam.addConstraint(9, node1, 1, slam.getRoot());

		slam.addConstraint(4, node2, 1, slam.getRoot());

		slam.addConstraint(-7, node2, 1, node1);

		System.out.println("Start Solve");
		slam.solve();

		slam.dump();

		fail("Not yet implemented");
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
