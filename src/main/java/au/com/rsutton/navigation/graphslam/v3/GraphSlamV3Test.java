package au.com.rsutton.navigation.graphslam.v3;

import static org.junit.Assert.fail;

import org.junit.Test;

public class GraphSlamV3Test
{

	@Test
	public void test()
	{
		GraphSlamV3<GraphSlamNodeImpl<DoubleWithMathOperators>, DoubleWithMathOperators> slam = new GraphSlamV3<>(
				getCtor());

		GraphSlamNodeImpl<DoubleWithMathOperators> node1 = slam.addNode("one", createValue(10), createValue(10), 1,
				slam.getRoot());

		GraphSlamNodeImpl<DoubleWithMathOperators> node2 = slam.addNode("two", createValue(3), createValue(3), 1,
				slam.getRoot());

		slam.addConstraint(createValue(9), node1, 1, slam.getRoot());

		slam.addConstraint(createValue(4), node2, 1, slam.getRoot());

		slam.addConstraint(createValue(-7), node2, 1, node1);

		System.out.println("Start Solve");
		slam.solve();

		slam.dump();

		fail("Not yet implemented");
	}

	@Test
	public void testm1()
	{
		GraphSlamV3<GraphSlamNodeImpl<DoubleWithMathOperators>, DoubleWithMathOperators> slam = new GraphSlamV3<>(
				getCtor());

		GraphSlamNodeImpl<DoubleWithMathOperators> node1 = slam.addNode("one", createValue(10), createValue(10), 1,
				slam.getRoot());

		GraphSlamNodeImpl<DoubleWithMathOperators> node2 = slam.addNode("two", createValue(13), createValue(3), 1,
				node1);

		System.out.println("Start Solve");
		slam.solve();

		slam.dump();

		fail("Not yet implemented");
	}

	DoubleWithMathOperators createValue(double value)
	{
		return new DoubleWithMathOperators(value);
	}

	private GraphSlamNodeConstructor<GraphSlamNodeImpl<DoubleWithMathOperators>, DoubleWithMathOperators> getCtor()
	{
		return new GraphSlamNodeConstructor<GraphSlamNodeImpl<DoubleWithMathOperators>, DoubleWithMathOperators>()
		{

			@Override
			public GraphSlamNodeImpl<DoubleWithMathOperators> construct(String name,
					DoubleWithMathOperators initialPosition)
			{
				return new GraphSlamNodeImpl<>(name, initialPosition, zero());
			}

			@Override
			public DoubleWithMathOperators zero()
			{
				return new DoubleWithMathOperators(0);
			}
		};
	}

	@Test
	public void test2()
	{
		GraphSlamV3<GraphSlamNodeImpl<PoseWithMathOperators>, PoseWithMathOperators> slam = new GraphSlamV3<>(
				getCtorPose());

		GraphSlamNodeImpl<PoseWithMathOperators> node1 = slam.addNode("one", createPoseValue(10, 0, 0),
				createPoseValue(10, 0, 0), 1, slam.getRoot());

		GraphSlamNodeImpl<PoseWithMathOperators> node2 = slam.addNode("two", createPoseValue(3, 0, 0),
				createPoseValue(3, 0, 0), 1, slam.getRoot());

		slam.addConstraint(createPoseValue(9, 0, 0), node1, 1, slam.getRoot());

		slam.addConstraint(createPoseValue(4, 0, 0), node2, 1, slam.getRoot());

		slam.addConstraint(createPoseValue(-7, 0, 0), node2, 1, node1);

		System.out.println("Start Solve");
		slam.solve();

		slam.dump();

		fail("Not yet implemented");
	}

	@Test
	public void test3()
	{
		GraphSlamV3<GraphSlamNodeImpl<PoseWithMathOperators>, PoseWithMathOperators> slam = new GraphSlamV3<>(
				getCtorPose());

		GraphSlamNodeImpl<PoseWithMathOperators> node1 = slam.addNode("one", createPoseValue(10, 0, 10),
				createPoseValue(10, 0, 10), 1, slam.getRoot());

		GraphSlamNodeImpl<PoseWithMathOperators> node2 = slam.addNode("two", createPoseValue(3, 0, 3),
				createPoseValue(3, 0, 3), 1, slam.getRoot());

		slam.addConstraint(createPoseValue(9, 0, 9), node1, 1, slam.getRoot());

		slam.addConstraint(createPoseValue(4, 0, 4), node2, 1, slam.getRoot());

		slam.addConstraint(createPoseValue(-7, 0, -7), node2, 1, node1);

		System.out.println("Start Solve");
		slam.solve();

		slam.dump();

		fail("Not yet implemented");
	}

	@Test
	public void test4()
	{
		GraphSlamV3<GraphSlamNodeImpl<PoseWithMathOperators>, PoseWithMathOperators> slam = new GraphSlamV3<>(
				getCtorPose());

		GraphSlamNodeImpl<PoseWithMathOperators> node1 = slam.addNode("one", createPoseValue(10, 0, 90),
				createPoseValue(10, 0, 90), 1, slam.getRoot());

		// GraphSlamNodeImpl<PoseWithMathOperators> node2 = slam.addNode("two",
		// createPoseValue(10, 0, 90), 1, node1);

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
