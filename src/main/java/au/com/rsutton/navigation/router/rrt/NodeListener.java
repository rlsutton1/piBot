package au.com.rsutton.navigation.router.rrt;

import java.awt.Color;

public interface NodeListener<T extends Pose<T>>
{

	void added(RrtNode<T> newNode, Color color, boolean forcePaint);

}
