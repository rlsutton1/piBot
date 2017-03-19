package au.com.rsutton.navigation.feature;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;

public class Spike
{
	double x;
	double y;
	double angle;

	SpikeDirectionAwayFromWall angleAwayFromWall;

	public Spike(double x2, double y2, double angle2, double angleAwayFromWall)
	{
		x = x2;
		y = y2;
		angle = angle2;
		this.angleAwayFromWall = SpikeDirectionAwayFromWall.ANTI_CLOCK_WISE;
		if (HeadingHelper.getChangeInHeading(angleAwayFromWall, angle) < 0)
		{
			this.angleAwayFromWall = SpikeDirectionAwayFromWall.CLOCK_WISE;
		}
	}

	public double getAngleAwayFromWall()
	{
		return angle + angleAwayFromWall.getAngleOffset();
	}

	public double getDistance(Spike spike)
	{
		return Math.sqrt(Math.pow(x - spike.x, 2) + Math.pow(y - spike.y, 2));
	}

	@Override
	public String toString()
	{
		return "Spike [x=" + x + ", y=" + y + ", angle=" + angle + ", angleAwayFromWall=" + angleAwayFromWall + "]";
	}

}