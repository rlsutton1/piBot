package au.com.rsutton.navigation.router.rrt;

import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;

interface Pose<T>
{
	public T getRandomPointInMapSpace(ProbabilityMapIIFc map, RttPhase rttPhase);

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
