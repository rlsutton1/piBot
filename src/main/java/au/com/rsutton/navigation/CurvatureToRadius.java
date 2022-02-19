package au.com.rsutton.navigation;

/**
 * https://www.math24.net/curvature-radius/
 * 
 * @author rsutton
 *
 */
public class CurvatureToRadius
{

	static double getCurvature(double firstDeriv, double secondDeriv)
	{
		double k = secondDeriv / (1 + (Math.pow(firstDeriv, 2.0)) * (3.0 / 2.0));

		return Math.abs(k);
	}

	static double getRadius(double curvature)
	{

		return 1.0 / curvature;
	}
}
