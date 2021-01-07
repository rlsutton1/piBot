package au.com.rsutton.navigation.router.rrt;

public interface Pose<T>
{
	public T getRandomPointInMapSpace(Array2d<Integer> map, int steps);

	public double calculateCost(T from);

	double getX();

	double getY();

	public T moveTowards(T location);

	public boolean canConnect(T pose2);

	public boolean canBridge(T pose2);

	public void updateParent(T parent);

	public T copy();

	public T invertDirection();

}
