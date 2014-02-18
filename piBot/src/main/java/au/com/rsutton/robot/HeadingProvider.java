package au.com.rsutton.robot;


public interface HeadingProvider
{

	int getHeading();

	void setCorrectedHeading(int heading);

	void addHeadingListener(HeadingListener robot);

	boolean isCalabrated();

}
