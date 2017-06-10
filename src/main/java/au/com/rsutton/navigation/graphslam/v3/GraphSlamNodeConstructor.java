package au.com.rsutton.navigation.graphslam.v3;

public interface GraphSlamNodeConstructor<T extends GraphSlamNode>
{

	T construct(String name, double initialPosition);

}
