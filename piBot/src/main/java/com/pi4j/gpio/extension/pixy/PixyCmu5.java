package com.pi4j.gpio.extension.pixy;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import au.com.rsutton.entryPoint.SynchronizedDeviceWrapper;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

public class PixyCmu5
{
	private static final int MAX_FRAMES = 10;
	private I2CBus bus;
	private I2CDevice magDevice;

	public void setup() throws IOException
	{

		// create I2C communications bus instance
		bus = I2CFactory.getInstance(1);

		// create I2C device instance
		magDevice = new SynchronizedDeviceWrapper(bus.getDevice(0x54));

	}

	public class Frame
	{
		// 0, 1 0 sync (0xaa55)
		// 2, 3 1 checksum (sum of all 16-bit words 2-6)
		// 4, 5 2 signature number
		// 6, 7 3 x center of object
		// 8, 9 4 y center of object
		// 10, 11 5 width of object
		// 12, 13 6 height of object

		int sync = 0;
		int checksum = 0;
		public int signature;
		public int xCenter;
		public int yCenter;
		public int width;
		public int height;
	}

	public List<Frame> getFrames() throws IOException
	{

		List<Frame> frames = new LinkedList<Frame>();
		byte[] bytes = new byte[14 * MAX_FRAMES];
		for (int i = 0; i < bytes.length; i++)
		{
			bytes[i] = 0;
		}

		// wait for sync byte
		if (magDevice.read() != 0x55)
		{
			return frames;
		}

		if (magDevice.read() != 0xaa)
		{
			return frames;
		}

		// don't lose the sync byte
		// bytes[0] = (byte) 0x55;
		int offset = 0;

		// read frames.
		int read = magDevice.read(bytes, offset, bytes.length);
		for (int r = 0; r < read; r += 14)
		{
			// System.out.print("f ");
			// for (int y = 0;y < 14;y++)
			// {
			// int t = bytes[r+y];
			// if (t < 0)
			// t = t + 256;
			// System.out.print(" " + Integer.toHexString(t));
			// }
			// System.out.println("");
			Frame frame = new Frame();
			frame.sync = convertBytesToInt(bytes[r + 1], bytes[r + 0]);
			// System.out.println("\nsync: "+Integer.toHexString(frame.sync));
			frame.checksum = convertBytesToInt(bytes[r + 3], bytes[r + 2]);
			frame.signature = convertBytesToInt(bytes[r + 5], bytes[r + 4]);
			frame.xCenter = convertBytesToInt(bytes[r + 7], bytes[r + 6]);
			frame.yCenter = convertBytesToInt(bytes[r + 9], bytes[r + 8]);
			frame.width = convertBytesToInt(bytes[r + 11], bytes[r + 10]);
			frame.height = convertBytesToInt(bytes[r + 13], bytes[r + 12]);

			if (frames.size() > MAX_FRAMES)
			{
				break;
			}
			// sync must equal =0x55aa;
			if (frame.sync != 0xaa55)
			{
				// System.out.println("Bad Pixy frame sync = " +
				// frame.sync+" "+frame.checksum);
				break;
			}
			// if the checksum is 0 or the checksum is a sync byte, then there
			// are no more frames.
			if (frame.checksum == 0 || frame.checksum == 0xaa55)
			{
				break;
			}
			frames.add(frame);
			offset = 0;
		}
		return frames;
	}

	public int convertBytesToInt(int msb, int lsb)
	{
		// System.out.println(Integer.toHexString(msb)+" "+Integer.toHexString(lsb));
		if (msb < 0)
			msb += 256;
		int value = msb * 256;

		if (lsb < 0)
		{
			// lsb should be unsigned
			value += 256;
		}
		value += lsb;
		return value;
	}

}
