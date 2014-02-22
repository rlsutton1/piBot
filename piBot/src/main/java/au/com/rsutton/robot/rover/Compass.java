package au.com.rsutton.robot.rover;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Compass implements Runnable
{
	private volatile int heading = 0;

	Compass()
	{
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this,
				100, 100, TimeUnit.MILLISECONDS);

	}

	public int getHeading()
	{
		return heading / 10;
	}

	@Override
	public void run()
	{
		heading = 1;

	}
}
