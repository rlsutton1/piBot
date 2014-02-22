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

public class QuadratureEncoding
{

	volatile int offset = 0;
	volatile int direction = 1;
	volatile QuadratureState lastState = QuadratureState.ONE;
	volatile int lastChange = 0;

	private Set<QuadratureListener> listeners = new HashSet<QuadratureListener>();

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

		lastChange = newState.getChange(lastState, lastChange);
		lastState = newState;
		offset += lastChange * direction;
		System.out.println(offset);

		for (QuadratureListener listener : listeners)
		{
			listener.quadraturePosition(offset);
		}
	}

	public short getValue()
	{
		return (short) offset;
	}

	public void addListener(QuadratureListener listener)
	{
		listeners.add(listener);

	}
}
