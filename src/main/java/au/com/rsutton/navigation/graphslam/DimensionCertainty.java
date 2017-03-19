package au.com.rsutton.navigation.graphslam;

public class DimensionCertainty implements Dimension
{

	double[] certainty;

	/**
	 * 
	 * @param x
	 * @param y
	 * @param thetaRadians
	 */
	public DimensionCertainty(double[] certainty)
	{
		this.certainty = certainty;
	}

	@Override
	public int getDimensions()
	{
		return certainty.length;
	}

	@Override
	public double get(int i)
	{
		if (i >= 0 && i < certainty.length)
		{
			return certainty[i];
		}

		throw new RuntimeException(i + " is not a valid dimension");
	}

	@Override
	public void set(int i, double value)
	{

		throw new RuntimeException(i + " is not a valid dimension");
	}

}
