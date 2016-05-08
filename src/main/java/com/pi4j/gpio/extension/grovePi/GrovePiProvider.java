package com.pi4j.gpio.extension.grovePi;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import au.com.rsutton.entryPoint.SynchronizedDeviceWrapper;

import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.GpioProviderBase;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.exception.InvalidPinException;
import com.pi4j.io.gpio.exception.UnsupportedPinModeException;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.impl.I2CBusImplBanana;

public class GrovePiProvider extends GpioProviderBase implements GpioProvider
{
	public static final String NAME = GrovePiProvider.class.getCanonicalName();
	public static final String DESCRIPTION = "Grove Pi Provider";

	private static final byte DIGITAL_READ_COMMAND = 1;
	private static final byte DIGITAL_WRITE_COMMAND = 2;

	private static final byte PIN_MODE_COMMAND = 5;
	private static final byte PIN_MODE_OUTPUT = 1;
	private static final byte PIN_MODE_INPUT = 0;

	private static final byte ANALOGUE_READ_COMMAND = 3;
	private static final byte ANALOGUE_WRITE_COMMAND = 4;

	private static final byte UNUSED = 0;

	private I2CBus bus;
	private I2CDevice device;

	Semaphore lock = new Semaphore(1, true);

	public GrovePiProvider(int busNumber, int address) throws IOException,
			InterruptedException
	{
		// TODO: reset GrovePi

		init();

		// create I2C communications bus instance
		// default = 0x04
		bus = I2CBusImplBanana.getBus(busNumber);

		// create I2C device instance
		device = bus.getDevice(address);
		device = new SynchronizedDeviceWrapper(device);

	}

	protected void init() throws IOException, InterruptedException
	{
		// // stuff for banana pi...
		// String exportPin8 = "gpio export 8 out";
		// String setPin8Hight = "gpio write 8 1";
		//
		// String exportPin10 = "gpio export 10 out";
		// String setPin10Hight = "gpio write 10 1";
		//
		// //String unexportPin8 = "gpio unexport 8";
		//
		// Runtime.getRuntime().exec(exportPin8);
		// Runtime.getRuntime().exec(setPin8Hight);
		//
		//
		// Runtime.getRuntime().exec(exportPin10);
		// Runtime.getRuntime().exec(setPin10Hight);
		//
		// //Runtime.getRuntime().exec(unexportPin8);
		//
		// Thread.sleep(1500);
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public void export(Pin pin, PinMode mode)
	{
		// make sure to set the pin mode
		super.export(pin, mode);
		setMode(pin, mode);

		sendPinModeCommand(pin, mode);

	}

	private void sendPinModeCommand(Pin pin, PinMode mode)
	{
		int value = PIN_MODE_INPUT;
		if (mode == PinMode.ANALOG_OUTPUT || mode == PinMode.PWM_OUTPUT
				|| mode == PinMode.DIGITAL_OUTPUT)
		{
			value = PIN_MODE_OUTPUT;
		}

		byte[] write = new byte[] {
				PIN_MODE_COMMAND, (byte) pin.getAddress(), (byte) value, UNUSED };

		managedIO(write, null);

	}

	@Override
	public void unexport(Pin pin)
	{
		super.unexport(pin);
		setMode(pin, PinMode.PWM_OUTPUT);
	}

	@Override
	public void setMode(Pin pin, PinMode mode)
	{
		if (!pin.getSupportedPinModes().contains(mode))
			throw new UnsupportedPinModeException(pin, mode);

		// cache mode
		getPinCache(pin).setMode(mode);
		sendPinModeCommand(pin, mode);

	}

	@Override
	public PinMode getMode(Pin pin)
	{
		return super.getMode(pin);
	}

	@Override
	public void setValue(Pin pin, double value)
	{
		if (hasPin(pin) == false)
			throw new InvalidPinException(pin);

		byte[] write = new byte[] {
				DIGITAL_WRITE_COMMAND, (byte) pin.getAddress(), (byte) value,
				UNUSED };
		managedIO(write, null);
		getPinCache(pin).setAnalogValue(value);

	}

	@Override
	public double getValue(Pin pin)
	{
		// validate
		if (hasPin(pin) == false)
			throw new InvalidPinException(pin);
		int v = 0;
		if (getPinCache(pin).getMode() == PinMode.ANALOG_INPUT)
		{
			v = analogRead(pin);

		} else
		{

			v = digitalRead(pin);
		}
		return v;

	}

	private int digitalRead(Pin pin)
	{
		byte[] write = new byte[] {
				DIGITAL_READ_COMMAND, (byte) pin.getAddress(), UNUSED, UNUSED };

		byte[] read = new byte[1];
		managedIO(write, read);

		return read[0] & 0xff;
	}

	private int analogRead(Pin pin)
	{
		byte[] write = new byte[] {
				ANALOGUE_READ_COMMAND, (byte) pin.getAddress(), UNUSED, UNUSED };

		byte[] read = new byte[3];
		managedIO(write, read);
		int v = read[2];
		if (v < 0)
		{
			v += 256;
		}
		v += read[1] * 256;
		return v;
	}

	@Override
	public void setPwm(Pin pin, int value)
	{

		// validate
		if (hasPin(pin) == false)
			throw new InvalidPinException(pin);

		byte[] write = new byte[] {
				ANALOGUE_WRITE_COMMAND, (byte) pin.getAddress(), (byte) value,
				UNUSED };
		managedIO(write, null);

		getPinCache(pin).setAnalogValue(value);
		// cache pin state
		getPinCache(pin).setPwmValue(value);

	}

	@Override
	public void setState(Pin pin, PinState state)
	{
		// validate
		if (hasPin(pin) == false)
			throw new InvalidPinException(pin);

		if (state == PinState.HIGH)
		{
			setValue(pin, 1);
		} else
		{
			setValue(pin, 0);
		}
	}

	@Override
	public void shutdown()
	{
		try
		{

			// close the I2C bus communication
			bus.close();
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	int managedIORead()
	{
		try
		{
			lock.acquire();

			int ctr = 0;
			while (ctr < 50)
			{
				try
				{
					ctr++;
					return device.read();
				} catch (Exception e)
				{

				}
			}
			if (ctr > 1)
			{
				System.out.println("Write took " + ctr + " attempts");
			}
			throw new RuntimeException("Too many failed read attempts");
		} catch (InterruptedException e1)
		{
			e1.printStackTrace();
			return 0;
		} finally
		{
			lock.release();
		}
	}

	void managedIO(byte[] write, byte[] read)
	{

		try
		{
			lock.acquire();
			if (write != null)
			{
				boolean success = false;
				int ctr = 0;
				while (ctr < 50 && !success)
				{
					try
					{
						ctr++;
						device.write(1, write, 0, write.length);
						success = true;
					} catch (Exception e)
					{

					}
				}
				if (!success)
				{
					throw new RuntimeException("Write failed");
				}
				if (ctr > 1)
				{
					System.out.println("Write took " + ctr + " attempts");
				}
			}

			if (read != null)
			{
				if (write != null)
				{
					Thread.sleep(1);
				}
				boolean success = false;
				int ctr = 0;
				while (ctr < 50 && !success)
				{
					try
					{
						ctr++;
						device.read(1, read, 0, read.length);
						success = true;
					} catch (Exception e)
					{

					}
				}
				if (!success)
				{
					throw new RuntimeException("Read failed");
				}
				if (ctr > 1)
				{
					System.out.println("Read took " + ctr + " attempts");
				}
			}
		} catch (InterruptedException e1)
		{
			e1.printStackTrace();
		} finally
		{
			lock.release();
		}
	}

}
