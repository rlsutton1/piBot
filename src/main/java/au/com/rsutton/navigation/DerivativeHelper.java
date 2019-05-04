package au.com.rsutton.navigation;

/**
 * https://en.wikipedia.org/wiki/Numerical_differentiation
 * 
 * @author rsutton
 *
 */
public class DerivativeHelper
{

	/**
	 * AKA speed
	 * 
	 * @param function
	 *            - (f(x+h)-f(x))/h
	 * @param x
	 * @param h
	 * @return
	 */
	static double getDerivative(BlackBoxFunction function, double x, double h)
	{
		return (function.of(x + h) - function.of(x)) / h;
	}

	/**
	 * AKA - acceleration
	 * 
	 * @param function
	 *            - f(x)
	 * @param x
	 * @param h
	 * @return
	 */
	static double getSecondDerivative(BlackBoxFunction function, double x, double h)
	{
		return getDerivative(e -> {
			return getDerivative(function, e, h);
		}, x, h);
	}

	/**
	 * AKA - jerk
	 * 
	 * @param function
	 *            - f(x)
	 * @param x
	 * @param h
	 * @return
	 */
	static double getThirdDerivative(BlackBoxFunction function, double x, double h)
	{
		return getDerivative(e -> {
			return getSecondDerivative(function, e, h);
		}, x, h);
	}

	/**
	 * (f(x+h)-f(x))/h
	 * 
	 * @param v1
	 *            = f(x)
	 * @param v2
	 *            = f(x+h)
	 * @param h
	 * @return
	 */
	static double getDerivative(double v1, double v2, double h)
	{
		return (v2 - v1) / h;
	}

	static double getSecondDerivative(double v1, double v2, double v3, double h1, double h2)
	{
		double va = getDerivative(v1, v2, h1);
		double vb = getDerivative(v2, v3, h2);
		return (vb - va) / ((h2 + h1) / 2.0);
	}
}
