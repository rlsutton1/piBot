package au.com.rsutton.robot.rover5.quadrature;

import com.pi4j.io.gpio.PinState;

public enum QuadratureState
{
	ONE(PinState.LOW, PinState.LOW)
	{
		@Override
		QuadratureState getTo()
		{
			return TWO;
		}

		@Override
		QuadratureState getFrom()
		{
			return FOUR;
		}
	},
	TWO(PinState.LOW, PinState.HIGH)
	{
		@Override
		QuadratureState getTo()
		{
			return THREE;
		}

		@Override
		QuadratureState getFrom()
		{
			return ONE;
		}
	},
	THREE(PinState.HIGH, PinState.HIGH)
	{
		@Override
		QuadratureState getTo()
		{
			return FOUR;
		}

		@Override
		QuadratureState getFrom()
		{
			return TWO;
		}
	},
	FOUR(PinState.HIGH, PinState.LOW)
	{
		@Override
		QuadratureState getTo()
		{
			return ONE;
		}

		@Override
		QuadratureState getFrom()
		{
			return THREE;
		}
	};

	QuadratureState from;
	QuadratureState to;
	private PinState a;
	private PinState b;

	QuadratureState(PinState a, PinState b)
	{
		this.a = a;
		this.b = b;
	}

	static QuadratureState getState(PinState a, PinState b)
	{
		QuadratureState state = null;

		for (QuadratureState test : QuadratureState.values())
		{
			if (test.a == a && test.b == b)
			{
				state = test;
				break;
			}
		}

		return state;
	}

	int getChange(QuadratureState lastState) throws QuadratureException
	{
		int ret = 0;

		if (this == lastState)
		{
			return 0;
		}else
		if (this.getFrom() == lastState)
		{
			// moving forward
			ret = 1;
		} else if (this.getTo() == lastState)
		{
			// moving backward
			ret = -1;
		} else
		{
			throw new QuadratureException();
//			// skipped a state, assume same direction as last step
//			ret = (int) (Math.signum(lastChange) * 2);
//			System.out.println("s");
//			//System.out.println("Skipped from "+lastState+" to "+this);
		}

		return ret;
	}

	abstract QuadratureState getTo();

	abstract QuadratureState getFrom();
}
