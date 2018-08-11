package au.com.rsutton.navigation.graphslam.v3;

import org.junit.Test;

import static junit.framework.TestCase.fail;

public class GraphSlamV3TestRotation {

	@Test
	public void test4()
	{
		GraphSlamV3<GraphSlamNodeImpl<PoseWithMathOperators>, PoseWithMathOperators> slam = new GraphSlamV3<>(
				getCtorPose());

		GraphSlamNodeImpl<PoseWithMathOperators> node1 = slam.addNode("one", createPoseValue(10, 0, 90),
				createPoseValue(10, 0, 90), 1, slam.getRoot());

		slam.addConstraint(createPoseValue(10, 0, 95), node1, 1, slam.getRoot());

		GraphSlamNodeImpl<PoseWithMathOperators> node2 = slam.addNode("two", createPoseValue(10, 0, 90),
				createPoseValue(10, 0, 90), 1, node1);

		// slam.addConstraint(createPoseValue(9, 0, 9), node1, 1,
		// slam.getRoot());
		//
		// slam.addConstraint(createPoseValue(4, 0, 4), node2, 1,
		// slam.getRoot());
		//
		// slam.addConstraint(createPoseValue(-7, 0, -7), node2, 1, node1);

		System.out.println("Start Solve");
		slam.solve();

		slam.dump();

		fail("Not yet implemented");
	}

	private PoseWithMathOperators createPoseValue(double x, double y, double angle)
	{
		return new PoseWithMathOperators(x, y, angle);
	}

	private GraphSlamNodeConstructor<GraphSlamNodeImpl<PoseWithMathOperators>, PoseWithMathOperators> getCtorPose()
	{
		return new GraphSlamNodeConstructor<GraphSlamNodeImpl<PoseWithMathOperators>, PoseWithMathOperators>()
		{

			@Override
			public PoseWithMathOperators zero()
			{
				return new PoseWithMathOperators(0, 0, 0);
			}

			@Override
			public GraphSlamNodeImpl<PoseWithMathOperators> construct(String name,
					PoseWithMathOperators initialPosition)
			{
				return new GraphSlamNodeImpl<>(name, initialPosition, zero());
			}
		};
	}

}
