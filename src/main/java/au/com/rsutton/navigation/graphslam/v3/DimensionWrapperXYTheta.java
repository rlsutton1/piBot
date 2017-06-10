package au.com.rsutton.navigation.graphslam.v3;

public class DimensionWrapperXYTheta
{
	GraphSlamNodeLinear nodeX;
	GraphSlamNodeLinear nodeY;
	GraphSlamNodeAngle nodeTheta;

	public DimensionWrapperXYTheta(GraphSlamNodeLinear nodeX, GraphSlamNodeLinear nodeY, GraphSlamNodeAngle nodeTheta)
	{
		this.nodeX = nodeX;
		this.nodeY = nodeY;
		this.nodeTheta = nodeTheta;
	}

	public double getX()
	{
		return nodeX.getPosition();
	}

	public double getY()
	{
		return nodeY.getPosition();
	}

	public double getTheta()
	{
		return nodeTheta.getPosition();
	}

	@Override
	public String toString()
	{
		return "DimensionWrapperXYTheta [nodeX=" + nodeX + ", nodeY=" + nodeY + ", nodeTheta=" + nodeTheta + "]";
	}

}
