package au.com.rsutton.navigation.feature;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;

public class Feature
{
	double x;
	double y;
	double angle;

	SpikeDirectionAwayFromWall angleAwayFromWall;
	private final FeatureType featureType;

	public Feature(double x2, double y2, double angle2, double angleAwayFromWall, FeatureType featureType)
	{
		x = x2;
		y = y2;
		angle = angle2;
		this.angleAwayFromWall = SpikeDirectionAwayFromWall.ANTI_CLOCK_WISE;
		if (HeadingHelper.getChangeInHeading(angleAwayFromWall, angle) < 0)
		{
			this.angleAwayFromWall = SpikeDirectionAwayFromWall.CLOCK_WISE;
		}
		this.featureType = featureType;
	}

	public Feature(double x2, double y2, double angle2, SpikeDirectionAwayFromWall angleAwayFromWall2,
			FeatureType featureType)
	{
		x = x2;
		y = y2;
		angle = angle2;
		this.angleAwayFromWall = angleAwayFromWall2;
		this.featureType = featureType;
	}

	public double getAngleAwayFromWall()
	{
		return angle + angleAwayFromWall.getAngleOffset();
	}

	public FeatureType getFeatureType()
	{
		return featureType;
	}

	public double getDistance(Feature spike)
	{
		return Math.sqrt(Math.pow(x - spike.x, 2) + Math.pow(y - spike.y, 2));
	}

	@Override
	public String toString()
	{
		return "Spike [x=" + x + ", y=" + y + ", angle=" + angle + ", angleAwayFromWall=" + angleAwayFromWall + "]";
	}

}