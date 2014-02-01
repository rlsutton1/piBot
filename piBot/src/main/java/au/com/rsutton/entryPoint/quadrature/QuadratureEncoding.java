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
	volatile int direction = -1;
	private Set<QuadratureListener> listeners = new HashSet<QuadratureListener>();

	public QuadratureEncoding(Pin a, Pin b, boolean direction)
	{
		if (direction)
		{
			this.direction = 1;
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
				PinState state = event.getState();
				if (state == PinState.HIGH)
				{
					if (pinB.getState() == PinState.LOW)
					{
						offset+=QuadratureEncoding.this.direction;
					} else
					{
						offset-=QuadratureEncoding.this.direction;
					}
				} else
				{
					if (pinB.getState() == PinState.HIGH)
					{
						offset+=QuadratureEncoding.this.direction;
					} else
					{
						offset-=QuadratureEncoding.this.direction;
					}
				}
				for (QuadratureListener listener:listeners)
				{
					listener.quadraturePosition(offset);
				}
//				System.out.println(pinA.getState() + " " + pinB.getState()
//						+ " " + offset);
			}
		};
		gpio.addListener(listenerA, pinA);

		GpioPinListenerDigital listenerB = new GpioPinListenerDigital()
		{

			@Override
			public void handleGpioPinDigitalStateChangeEvent(
					GpioPinDigitalStateChangeEvent event)
			{
				PinState state = event.getState();
				if (state == PinState.HIGH)
				{
					if (pinA.getState() == PinState.HIGH)
					{
						offset+=QuadratureEncoding.this.direction;
					} else
					{
						offset-=QuadratureEncoding.this.direction;
					}
				} else
				{
					if (pinA.getState() == PinState.LOW)
					{
						offset+=QuadratureEncoding.this.direction;
					} else
					{
						offset-=QuadratureEncoding.this.direction;
					}
				}
				for (QuadratureListener listener:listeners)
				{
					listener.quadraturePosition(offset);
				}
//				System.out.println(pinA.getState() + " " + pinB.getState()
//						+ " " + offset);
			}
		};
		gpio.addListener(listenerB, pinB);

	}

	public short getValue()
	{
		return (short) offset;
	}

	public void addListener(QuadratureListener listener)
	{
		listeners .add(listener);
		
	}
}
