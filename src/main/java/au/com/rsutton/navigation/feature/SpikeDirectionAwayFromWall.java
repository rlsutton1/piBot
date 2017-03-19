package au.com.rsutton.navigation.feature;

public enum SpikeDirectionAwayFromWall
{
	ANTI_CLOCK_WISE(90), CLOCK_WISE(-90);

	double offset;

	private SpikeDirectionAwayFromWall(double offset)
	{
		this.offset = offset;
	}

	public double getAngleOffset()
	{
		return offset;
	}

}
