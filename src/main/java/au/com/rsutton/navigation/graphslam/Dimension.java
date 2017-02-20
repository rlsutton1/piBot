package au.com.rsutton.navigation.graphslam;

public interface Dimension
{
	/**
	 * the number of dimesions
	 * 
	 * @return
	 */
	int getDimensions();

	/**
	 * get the value for the i'th dimension
	 * 
	 * @param i
	 * @return
	 */
	double get(int i);

	/**
	 * set the value for the i'th dimension
	 * 
	 * @param i
	 * @param value
	 */
	void set(int i, double value);
}
