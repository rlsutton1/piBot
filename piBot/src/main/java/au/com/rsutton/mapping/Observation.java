package au.com.rsutton.mapping;

public interface Observation
{

	double getAccuracy();

	double getX();

	double getY();

	LocationStatus getStatus();

	void seenAgain();

}
