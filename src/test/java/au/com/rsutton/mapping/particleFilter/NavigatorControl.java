package au.com.rsutton.mapping.particleFilter;

public interface NavigatorControl
{

	/**
	 * stop immediately
	 */
	void stop();

	/**
	 * start or resume the journey
	 */
	void go();

	/**
	 * calculate a new route to the provided destination using the current map
	 * state
	 * 
	 * @param x
	 * @param y
	 * @param heading
	 */
	void calculateRouteTo(int x, int y, double heading);

	boolean hasReachedDestination();

	boolean isStuck();

	boolean isStopped();

}
