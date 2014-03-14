package com.pi4j.gpio.extension.adafruit;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import au.com.rsutton.entryPoint.SynchronizedDeviceWrapper;

import com.google.common.base.Preconditions;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

public class ADS1115 implements Runnable
{

	private static int ADS1115_CONVERSIONDELAY = 8;
	/*
	 * =========================================================================
	 * POINTER REGISTER
	 * -----------------------------------------------------------------------
	 */
	private static int ADS1015_REG_POINTER_CONVERT = 0;
	private static int ADS1015_REG_POINTER_CONFIG = 1;
	private static int ADS1015_REG_POINTER_LOWTHRESH = 2;
	private static int ADS1015_REG_POINTER_HITHRESH = 3;

	/*
	 * =========================================================================
	 * CONFIG REGISTER
	 * -----------------------------------------------------------------------
	 */
	private static int ADS1015_REG_CONFIG_OS_MASK = 0x8000;
	private static int ADS1015_REG_CONFIG_OS_SINGLE = 0x8000; // Write: Set to
																// start a
																// single-conversion
	private static int ADS1015_REG_CONFIG_OS_BUSY = 0x0000; // Read: Bit = 0
															// when conversion
															// is in progress
	private static int ADS1015_REG_CONFIG_OS_NOTBUSY = 0x8000; // Read: Bit = 1
																// when device
																// is not
																// performing a
																// conversion

	private static int ADS1015_REG_CONFIG_MUX_MASK = 0x7000;
	private static int ADS1015_REG_CONFIG_MUX_DIFF_0_1 = 0x0000; // Differential
																	// P = AIN0,
																	// N = AIN1
																	// (default)
	private static int ADS1015_REG_CONFIG_MUX_DIFF_0_3 = 0x1000; // Differential
																	// P = AIN0,
																	// N = AIN3
	private static int ADS1015_REG_CONFIG_MUX_DIFF_1_3 = 0x2000; // Differential
																	// P = AIN1,
																	// N = AIN3
	private static int ADS1015_REG_CONFIG_MUX_DIFF_2_3 = 0x3000; // Differential
																	// P = AIN2,
																	// N = AIN3
	private static int ADS1015_REG_CONFIG_MUX_SINGLE_0 = 0x4000; // Single-ended
																	// AIN0
	private static int ADS1015_REG_CONFIG_MUX_SINGLE_1 = 0x5000; // Single-ended
																	// AIN1
	private static int ADS1015_REG_CONFIG_MUX_SINGLE_2 = 0x6000; // Single-ended
																	// AIN2
	private static int ADS1015_REG_CONFIG_MUX_SINGLE_3 = 0x7000; // Single-ended
																	// AIN3

	private static int ADS1015_REG_CONFIG_PGA_MASK = 0x0E00;
	private static int ADS1015_REG_CONFIG_PGA_6_144V = 0x0000; // +/-6.144V
																// range
	private static int ADS1015_REG_CONFIG_PGA_4_096V = 0x0200; // +/-4.096V
																// range
	private static int ADS1015_REG_CONFIG_PGA_2_048V = 0x0400; // +/-2.048V
																// range
																// (default)
	private static int ADS1015_REG_CONFIG_PGA_1_024V = 0x0600; // +/-1.024V
																// range
	private static int ADS1015_REG_CONFIG_PGA_0_512V = 0x0800; // +/-0.512V
																// range
	private static int ADS1015_REG_CONFIG_PGA_0_256V = 0x0A00; // +/-0.256V
																// range

	private static int ADS1015_REG_CONFIG_MODE_MASK = 0x0100;
	private static int ADS1015_REG_CONFIG_MODE_CONTIN = 0x0000; // Continuous
																// conversion
																// mode
	private static int ADS1015_REG_CONFIG_MODE_SINGLE = 0x0100; // Power-down
																// single-shot
																// mode
																// (default)

	private static int ADS1015_REG_CONFIG_DR_MASK = 0x00E0;
	private static int ADS1015_REG_CONFIG_DR_128SPS = 0x0000; // 128 samples per
																// second
	private static int ADS1015_REG_CONFIG_DR_250SPS = 0x0020; // 250 samples per
																// second
	private static int ADS1015_REG_CONFIG_DR_490SPS = 0x0040; // 490 samples per
																// second
	private static int ADS1015_REG_CONFIG_DR_920SPS = 0x0060; // 920 samples per
																// second
	private static int ADS1015_REG_CONFIG_DR_1600SPS = 0x0080; // 1600 samples
																// per second
																// (default)
	private static int ADS1015_REG_CONFIG_DR_2400SPS = 0x00A0; // 2400 samples
																// per second
	private static int ADS1015_REG_CONFIG_DR_3300SPS = 0x00C0; // 3300 samples
																// per second

	private static int ADS1015_REG_CONFIG_CMODE_MASK = 0x0010;
	private static int ADS1015_REG_CONFIG_CMODE_TRAD = 0x0000; // Traditional
																// comparator
																// with
																// hysteresis
																// (default)
	private static int ADS1015_REG_CONFIG_CMODE_WINDOW = 0x0010; // Window
																	// comparator
	private static int ADS1015_REG_CONFIG_CPOL_MASK = 0x0008;
	private static int ADS1015_REG_CONFIG_CPOL_ACTVLOW = 0x0000; // ALERT/RDY
																	// pin is
																	// low when
																	// active
																	// (default)
	private static int ADS1015_REG_CONFIG_CPOL_ACTVHI = 0x0008;; // ALERT/RDY
																	// pin is
																	// high when
																	// active

	private static int ADS1015_REG_CONFIG_CLAT_MASK = 0x0004; // Determines if
																// ALERT/RDY pin
																// latches once
																// asserted
	private static int ADS1015_REG_CONFIG_CLAT_NONLAT = 0x0000; // Non-latching
																// comparator
																// (default)
	private static int ADS1015_REG_CONFIG_CLAT_LATCH = 0x0004; // Latching
																// comparator

	private static int ADS1015_REG_CONFIG_CQUE_MASK = 0x0003;
	private static int ADS1015_REG_CONFIG_CQUE_1CONV = 0x0000; // Assert
																// ALERT/RDY
																// after one
																// conversions
	private static int ADS1015_REG_CONFIG_CQUE_2CONV = 0x0001; // Assert
																// ALERT/RDY
																// after two
																// conversions
	private static int ADS1015_REG_CONFIG_CQUE_4CONV = 0x0002; // Assert
																// ALERT/RDY
																// after four
																// conversions
	private static int ADS1015_REG_CONFIG_CQUE_NONE = 0x0003; // Disable the
																// comparator
																// and put
																// ALERT/RDY in
																// high state
																// (default)
	/* ========================================================================= */

	// Instance-specific properties
	int m_i2cAddress;
	private I2CBus bus;
	private I2CDevice device;

	public ADS1115(int busNumber, int i2cAddress) throws IOException
	{
		m_i2cAddress = i2cAddress;

		// create I2C communications bus instance
		bus = I2CFactory.getInstance(busNumber);

		// create I2C device instance
		device = new SynchronizedDeviceWrapper(bus.getDevice(i2cAddress));

		new Thread(this).start();

	}

	private class Request
	{
		public Request(int port2, AnalogueValueCallback callback2)
		{
			this.port = port2;
			this.callback = callback2;
		}

		int port;
		AnalogueValueCallback callback;
	}

	LinkedBlockingQueue<Request> queue = new LinkedBlockingQueue<>();

	private boolean stop;

	public void getValue(int port, AnalogueValueCallback callback)
	{
		Preconditions.checkArgument(port >= 0 && port <= 3,
				"valid ports are 0 - 4");
		if (queue.size() < 10)
		{
			queue.add(new Request(port, callback));
		}
	}

	@Override
	public void run()
	{

		while (!stop)
		{
			try
			{
				Request request = queue.take();
				// read the values converted from the last config

				Integer config = getConfigDefaults();

				// Set single-ended input channel
				switch (request.port)
				{
				case (0):
					config |= ADS1015_REG_CONFIG_MUX_SINGLE_0;
					break;
				case (1):
					config |= ADS1015_REG_CONFIG_MUX_SINGLE_1;
					break;
				case (2):
					config |= ADS1015_REG_CONFIG_MUX_SINGLE_2;
					break;
				case (3):
					config |= ADS1015_REG_CONFIG_MUX_SINGLE_3;
					break;
				}

				// Set 'start single-conversion' bit
				config |= ADS1015_REG_CONFIG_OS_SINGLE;

				// Write config register to the ADC

				write16Bits(ADS1015_REG_POINTER_CONFIG, config);
				Thread.sleep(10);
				request.callback
						.analogValue(read16Bits(ADS1015_REG_POINTER_CONVERT));
			} catch (IOException e)
			{
				e.printStackTrace();
			} catch (InterruptedException e)
			{
				stop = true;
			}
		}
	}

	private Integer getConfigDefaults()
	{
		// Start with default values
		Integer config = ADS1015_REG_CONFIG_CQUE_NONE | // Disable the
														// comparator
														// (default
														// val)
				ADS1015_REG_CONFIG_CLAT_NONLAT | // Non-latching
													// (default
													// val)
				ADS1015_REG_CONFIG_CPOL_ACTVLOW | // Alert/Rdy active
													// low
													// (default val)
				ADS1015_REG_CONFIG_CMODE_TRAD | // Traditional
												// comparator
												// (default val)
				ADS1015_REG_CONFIG_DR_920SPS | // 1600 samples per
												// second
												// (default)
				ADS1015_REG_CONFIG_MODE_SINGLE; // Single-shot mode
												// (default)

		// Set PGA/voltage range
		config |= ADS1015_REG_CONFIG_PGA_4_096V; // +/- 6.144V range
		// (limited to
		// VDD +0.3V max!)
		return config;
	}

	int read16Bits(int address) throws IOException
	{
		byte[] buffer = new byte[2];
		device.read(address, buffer, 0, buffer.length);
		int msb = buffer[0];
		int lsb = buffer[1];
		if (lsb < 0)
			lsb = 256 + lsb;

		return ((msb << 8) + lsb);

	}

	void write16Bits(int address, int value) throws IOException
	{
		byte[] buffer = new byte[2];
		buffer[0] = (byte) (value << 8);// msb
		buffer[1] = (byte) (value & 0xFF); // lsb
		device.write(address, buffer, 0, buffer.length);

	}

}
