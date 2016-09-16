import static org.junit.Assert.*;

import org.junit.Test;

public class ConstantCurrent
{

	@Test
	public void testOhmic()
	{
		for (double i = 1; i < 10000; i += 10)
		{
			TestLoad load = new Ohmic(i);
			runTest(load);
			assertTrue("Exceeded Limit " + load.getMaxCurrent(), load.getMaxCurrent() < 0.25);
			assertTrue("too many steps " + load.getSteps(), load.getSteps() < 100);

		}

	}

	@Test
	public void testLed()
	{
		TestLoad load = new Led(0.00966, 1.818, -1.157);
		runTest(load);

		assertTrue("Exceeded Limit " + load.getMaxCurrent(), load.getMaxCurrent() < 0.25);
		assertTrue("too many steps " + load.getSteps(), load.getSteps() < 30);

	}

	private void runTest(TestLoad load)
	{
		double targetCurrent = 0.001;
		double voltage = 0.00;
		double diff = 0.00;
		double accuracy = 0.0001;

		double step = 0.1;

		double top = 0.0;
		double bottom = 0.0;
		boolean reachedTop = false;

		do
		{
			double current = load.getCurrent(voltage);
			diff = targetCurrent - current;

			
			double direction = Math.signum(diff);
			if (Math.abs(diff) > accuracy)
			if (direction > 0)
			{
				if (reachedTop == false)
				{
					top += step;
					voltage = top;
				} else
				{
					bottom = voltage;
					voltage = (top + bottom) / 2.0;
				}
			} else
			{
				reachedTop = true;
				top = voltage;
				voltage = (top + bottom) / 2.0;
			}

			
			//System.out.println(top + " " + bottom + " " + voltage+" I: "+current);

		} while (Math.abs(diff) > accuracy);
		System.out.println("V: " + voltage + "  I:  " + diff);
	}

	interface TestLoad
	{
		double getCurrent(double voltage);

		int getSteps();

		double getMaxCurrent();
	}

	class Ohmic implements TestLoad
	{

		private double resistance;
		private int steps;
		private double max = 0;

		Ohmic(double resistance)
		{
			this.resistance = resistance;
		}

		@Override
		public double getCurrent(double voltage)
		{
			steps++;
			double i = voltage / resistance;
			max = Math.max(max, i);
			return i;
		}

		@Override
		public int getSteps()
		{
			return steps;
		}

		@Override
		public double getMaxCurrent()
		{
			return max;
		}

	}

	class Led implements TestLoad
	{

		private double a = 0.00966;
		private double b = 1.818;
		private double c = -1.157;
		private int steps;
		private double max = 0;

		Led(double a, double b, double c)
		{
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public double getCurrent(double voltage)
		{
			steps++;
			double i = Math.max(0, (a * Math.pow(voltage, 2)) + (voltage * b) + c);
			max = Math.max(max, i);
			return i;
		}

		@Override
		public int getSteps()
		{
			return steps;
		}

		@Override
		public double getMaxCurrent()
		{
			return max;
		}

	}
}
