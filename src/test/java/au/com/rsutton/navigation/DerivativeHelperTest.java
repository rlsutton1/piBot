package au.com.rsutton.navigation;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DerivativeHelperTest
{

	@Test
	public void testpos()
	{
		new DerivativeHelper();
		double value = DerivativeHelper.getDerivative(new BlackBoxFunction()
		{

			@Override
			public double of(double x)
			{
				return x * 2.0;
			}
		}, 5.0, 0.1);

		assertTrue("got " + value, Math.abs(2.0 - value) < 0.01);
	}

	@Test
	public void testposNew()
	{
		new DerivativeHelper();
		double value = DerivativeHelper.getDerivative(new BlackBoxFunction()
		{

			@Override
			public double of(double x)
			{
				return x * 2.0;
			}
		}, 5.0, 0.1);

		double value2 = DerivativeHelper.getDerivative(5.0, 5.2, 0.1);

		assertTrue("got " + value, Math.abs(value2 - value) < 0.0001);
	}

	@Test
	public void testPowNew()
	{
		new DerivativeHelper();
		double value = DerivativeHelper.getSecondDerivative(new BlackBoxFunction()
		{

			@Override
			public double of(double x)
			{
				return Math.pow(x, 2);
			}
		}, 5.0, 0.1);

		double value2 = DerivativeHelper.getSecondDerivative(Math.pow(5, 2), Math.pow(5.5, 2), Math.pow(5.6, 2), 0.5,
				0.1);

		assertTrue("got " + value + " and " + value2, Math.abs(value2 - value) < 0.0001);
	}

	@Test

	public void testneg()
	{
		new DerivativeHelper();
		double value = DerivativeHelper.getDerivative(new BlackBoxFunction()
		{

			@Override
			public double of(double x)
			{
				return x / -2.0;
			}
		}, 5.0, 0.1);

		assertTrue("got " + value, Math.abs(-0.5 - value) < 0.01);
	}

	@Test
	public void test2ndDeriv()
	{
		new DerivativeHelper();
		double value = DerivativeHelper.getSecondDerivative(new BlackBoxFunction()
		{

			@Override
			public double of(double x)
			{
				return x * 2.0;
			}
		}, 5.0, 0.1);

		assertTrue("got " + value, Math.abs(0 - value) < 0.01);
	}

	@Test
	public void test2ndDerivPwr()
	{
		new DerivativeHelper();
		double value = DerivativeHelper.getSecondDerivative(new BlackBoxFunction()
		{

			@Override
			public double of(double x)
			{
				double value = Math.pow(x, 2.0);
				return value;
			}
		}, 5.0, 0.1);

		assertTrue("got " + value, Math.abs(2.0 - value) < 0.01);
	}

	@Test
	public void test3rdDerivSqr()
	{
		new DerivativeHelper();
		double value = DerivativeHelper.getThirdDerivative(new BlackBoxFunction()
		{

			@Override
			public double of(double x)
			{
				double value = Math.sqrt(x);
				return value;
			}
		}, 5.0, 0.1);

		assertTrue("got " + value, Math.abs(0 - value) < 0.01);
	}

	@Test
	public void test3rdDerivPow()
	{
		new DerivativeHelper();
		double value = DerivativeHelper.getThirdDerivative(new BlackBoxFunction()
		{

			@Override
			public double of(double x)
			{
				double value = Math.pow(x, 2);
				return value;
			}
		}, 5.0, 0.1);

		assertTrue("got " + value, Math.abs(0 - value) < 0.01);
	}

}
