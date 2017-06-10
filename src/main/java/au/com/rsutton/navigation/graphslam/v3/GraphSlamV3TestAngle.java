package au.com.rsutton.navigation.graphslam.v3;

import static org.junit.Assert.fail;

import org.junit.Test;

public class GraphSlamV3TestAngle
{

	@Test
	public void test()
	{
		GraphSlamV3<GraphSlamNodeAngle> slam = new GraphSlamV3<>(getCtor());

		slam.move("p1", 0, 1);

		GraphSlamNodeAngle node0 = slam.addNode("zero", 380, 1);

		slam.addConstraint(20, node0, 1);

		slam.move("p2", 720, 1);

		slam.addConstraint(20, node0, 1);

		GraphSlamNodeAngle node1 = slam.addNode("one", -180, 1);

		slam.addConstraint(180, node1, 1);

		slam.move("p3", 190, 1);

		slam.addConstraint(0, node1, 1);

		//
		// slam.addConstraint(180, node1);
		//
		// slam.addConstraint(-270, node2);
		//
		// slam.move("p2", 180);
		//
		// slam.addConstraint(-90, node2);

		slam.solve();

	
		fail("Not yet implemented");
	}

	private GraphSlamNodeConstructor<GraphSlamNodeAngle> getCtor()
	{
		return new GraphSlamNodeConstructor<GraphSlamNodeAngle>()
		{

			@Override
			public GraphSlamNodeAngle construct(String name, double initialPosition)
			{
				return new GraphSlamNodeAngle(name, initialPosition);
			}
		};
	}

}
