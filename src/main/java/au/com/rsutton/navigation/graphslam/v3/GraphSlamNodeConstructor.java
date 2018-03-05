package au.com.rsutton.navigation.graphslam.v3;

public interface GraphSlamNodeConstructor<N extends GraphSlamNode<V>, V extends MathOperators<V>>
{

	N construct(String name, V initialPosition);

	V zero();

}
