package au.com.rsutton.gradientdescent;

import java.util.Arrays;

public class GradientDescent
{

	public interface GDFunction
	{
		double getValue(double[] parameters);
	}

	static class TestFunction implements GDFunction
	{

		@Override
		public double getValue(double[] parameters)
		{
			// simple parabola
			return Math.pow(parameters[0] - 4, 2)

					+ Math.pow(parameters[1] - 2, 2)

					+ Math.log(Math.abs(parameters[2]));
		}

	}

	private GDFunction function;
	private double[] parameters;

	public GradientDescent(GDFunction function, double[] parameters)
	{
		this.function = function;
		this.parameters = parameters;
	}

	double[] copyParameters()
	{
		double[] params = new double[parameters.length];
		for (int i = 0; i < parameters.length; i++)
		{
			params[i] = parameters[i];
		}
		return params;
	}

	public double[] descend(double alpha, double h)
	{

		// h is spacing for derivative

		// alpha is the learning rate

		int remainingIterations = 10000;

		double threshold = 0.0000001;

		double bestValue = function.getValue(parameters);
		double[] best = copyParameters();

		double md = 1;
		int uselessIterations = 0;
		while (uselessIterations < 10 && Math.abs(md) > threshold && remainingIterations > 0)
		{
			remainingIterations--;
			md = 0;
			double[] newValues = copyParameters();
			double[] p1 = copyParameters();
			for (int i = 0; i < parameters.length; i++)
			{
				double[] p2 = copyParameters();
				p2[i] = p2[i] + h;
				double derivative = getDerivative(function, p1, p2, h);
				md = Math.max(md, Math.abs(derivative));

				if (Math.abs(derivative) > threshold)
				{
					newValues[i] = p1[i] - (Math.signum(derivative) * h);
				}
			}

			for (int i = 0; i < parameters.length; i++)
			{
				parameters[i] = newValues[i];
			}

			System.out.println(function.getValue(parameters) + " -> " + Arrays.toString(parameters));

			// store best result for early termination
			if (function.getValue(parameters) < bestValue)
			{
				bestValue = function.getValue(parameters);
				best = copyParameters();
				uselessIterations = 0;
			}
			uselessIterations++;

		}
		System.out.println(function.getValue(best) + " -> " + Arrays.toString(best) + " ... remaining iterations "
				+ remainingIterations);

		return best;
	}

	/**
	 * 
	 * @param function
	 * @param p1
	 * @param p2
	 * @param h
	 *            distance between p1 and p2
	 * @return
	 */
	double getDerivative(GDFunction function, double[] p1, double[] p2, double h)
	{
		double v1 = function.getValue(p1);
		double v2 = function.getValue(p2);

		return (v2 - v1) / h;
	}
}
