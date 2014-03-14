package au.com.rsutton.entryPoint.quadrature;

import java.util.HashSet;
import java.util.Set;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class QuadratureEncoding implements QuadratureProvider
{

	volatile int offset = 0;
	volatile int direction = 1;
	volatile QuadratureState lastState = QuadratureState.ONE;
	volatile int lastChange = 0;
	private double errors;
	private double steps;

	public QuadratureEncoding(Pin a, Pin b, boolean invertDirection)
	{
		if (invertDirection)
		{
			this.direction *= -1;
		}
		final GpioController gpio = GpioFactory.getInstance();

		final GpioPinDigitalInput pinA = gpio.provisionDigitalInputPin(a,
				PinPullResistance.PULL_DOWN);

		final GpioPinDigitalInput pinB = gpio.provisionDigitalInputPin(b,
				PinPullResistance.PULL_DOWN);

		GpioPinListenerDigital listenerA = new GpioPinListenerDigital()
		{

			@Override
			public void handleGpioPinDigitalStateChangeEvent(
					GpioPinDigitalStateChangeEvent event)
			{
				PinState a = event.getState();
				PinState b = pinB.getState();

				applyStateChange(a, b);
				// System.out.println(pinA.getState() + " " + pinB.getState()
				// + " " + offset);
			}
		};
		gpio.addListener(listenerA, pinA);

		GpioPinListenerDigital listenerB = new GpioPinListenerDigital()
		{

			@Override
			public void handleGpioPinDigitalStateChangeEvent(
					GpioPinDigitalStateChangeEvent event)
			{
				PinState a = pinA.getState();
				PinState b = event.getState();

				applyStateChange(a, b);
				// System.out.println(pinA.getState() + " " + pinB.getState()
				// + " " + offset);
			}

		};
		gpio.addListener(listenerB, pinB);

	}

	private void applyStateChange(PinState a, PinState b)
	{
		QuadratureState newState = QuadratureState.getState(a, b);

		try
		{
			steps++;
			lastChange = newState.getChange(lastState);
		} catch (QuadratureException e)
		{
			errors++;

			offset += lastChange * direction;
			// errors diminishes every click, after 8 clicks an error will have
			// diminished to 0.5.
			// so 2 errors in 8 clicks becomes 1.5/8 = 0.1875
			if (errors / steps > 0.05)
			{
				System.out.println("Quad errors " + errors + " / " + steps
						+ " = " + errors / steps);
			}
		}

		lastState = newState;
		offset += lastChange * direction;
	}

	@Override
	public long getValue()
	{
		return (short) offset;
	}

}
