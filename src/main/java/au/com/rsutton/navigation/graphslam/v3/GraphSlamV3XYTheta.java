package au.com.rsutton.navigation.graphslam.v3;

import java.util.LinkedList;
import java.util.List;

import au.com.rsutton.navigation.graphslam.DimensionCertainty;

public class GraphSlamV3XYTheta
{

	GraphSlamV3<GraphSlamNodeLinear> slamX;
	GraphSlamV3<GraphSlamNodeLinear> slamY;
	GraphSlamV3<GraphSlamNodeAngle> slamTheta;

	List<DimensionWrapperXYTheta> positions = new LinkedList<>();

	public GraphSlamV3XYTheta(double x, double y, double theta, DimensionCertainty certainty)
	{
		slamX = new GraphSlamV3<>(getLinearCtor());
		slamY = new GraphSlamV3<>(getLinearCtor());
		slamTheta = new GraphSlamV3<>(getAngleCtor());
		setNewLocation(x, y, theta, certainty);

	}

	private GraphSlamNodeConstructor<GraphSlamNodeLinear> getLinearCtor()
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

	private GraphSlamNodeConstructor<GraphSlamNodeAngle> getAngleCtor()
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

	public List<DimensionWrapperXYTheta> getPositions()
	{

		return positions;
	}

	public DimensionWrapperXYTheta setNewLocation(double x, double y, double theta, DimensionCertainty certainty)
	{
		solve();
		GraphSlamNodeLinear xNode = slamX.move(x, certainty.get(0));
		GraphSlamNodeLinear yNode = slamY.move(y, certainty.get(2));
		GraphSlamNodeAngle thetaNode = slamTheta.move(theta, certainty.get(0));

		return new DimensionWrapperXYTheta(xNode, yNode, thetaNode);

	}

	public DimensionWrapperXYTheta add(String label, double x, double y, double theta, DimensionCertainty certainty)
	{

		GraphSlamNodeLinear xNode = slamX.addNode("x-" + label, x, certainty.get(0));
		GraphSlamNodeLinear yNode = slamY.addNode("y-" + label, y, certainty.get(2));
		GraphSlamNodeAngle thetaNode = slamTheta.addNode("theta-" + label, theta, certainty.get(0));

		return new DimensionWrapperXYTheta(xNode, yNode, thetaNode);
	}

	public DimensionWrapperXYTheta add(double x, double y, double theta, DimensionCertainty certainty)
	{

		GraphSlamNodeLinear xNode = slamX.addNode(x, certainty.get(0));
		GraphSlamNodeLinear yNode = slamY.addNode(y, certainty.get(2));
		GraphSlamNodeAngle thetaNode = slamTheta.addNode(theta, certainty.get(0));

		return new DimensionWrapperXYTheta(xNode, yNode, thetaNode);
	}

	public void update(DimensionWrapperXYTheta node, double x, double y, double theta, DimensionCertainty certainty)
	{
		slamX.addConstraint(x, node.nodeX, certainty.get(0));
		slamY.addConstraint(y, node.nodeY, certainty.get(0));
		slamTheta.addConstraint(theta, node.nodeTheta, certainty.get(0));

	}

	public void dumpPositions()
	{
		slamX.dump();
		slamY.dump();
		slamTheta.dump();
	}

	public void solve()
	{
		slamX.solve();
		slamY.solve();
		slamTheta.solve();

	}

	public void dumpPositionsY()
	{
		slamY.dump();
	}
}
