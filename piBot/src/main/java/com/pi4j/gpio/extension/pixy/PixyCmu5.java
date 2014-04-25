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
	private static final int FRAME_SIZE = 14;
	private static final int MAX_FRAMES = 20;
	private I2CBus bus;
	private I2CDevice pixyDevice;

	public void setup() throws IOException
	{

		// create I2C communications bus instance
		bus = I2CFactory.getInstance(1);

		// create I2C device instance
		pixyDevice = new SynchronizedDeviceWrapper(bus.getDevice(0x54));

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

	public Frame getFrame(byte[] bytes)
	{
		Frame frame = new Frame();
		frame.sync = convertBytesToInt(bytes[1], bytes[0]);
		// System.out.println("\nsync: "+Integer.toHexString(frame.sync));
		frame.checksum = convertBytesToInt(bytes[3], bytes[2]);

		// if the checksum is 0 or the checksum is a sync byte, then there
		// are no more frames.
		if (frame.checksum == 0 || frame.checksum == 0xaa55)
		{
			return null;
		}
		frame.signature = convertBytesToInt(bytes[5], bytes[4]);
		frame.xCenter = convertBytesToInt(bytes[7], bytes[6]);
		frame.yCenter = convertBytesToInt(bytes[9], bytes[8]);
		frame.width = convertBytesToInt(bytes[11], bytes[10]);
		frame.height = convertBytesToInt(bytes[13], bytes[12]);

		return frame;
	}

	public List<Frame> getFrames() throws IOException
	{

		List<Frame> frames = new LinkedList<Frame>();
		byte[] bytes = new byte[FRAME_SIZE * MAX_FRAMES];

		// read data from pixy
		int bytesRead = pixyDevice.read(bytes, 0, bytes.length);

		System.out.println("Bytes read " + bytesRead);

		// search for sync
		for (int byteOffset = 0; byteOffset < bytesRead - (FRAME_SIZE - 1);)
		{
			
			int b1 = bytes[byteOffset];
			if (b1 < 0)
			{
				b1+=256;
			}
			int b2 = bytes[byteOffset+1];
			if (b2 < 0)
			{
				b2+=256;
			}

			if (b1 == 0x55 && b2 == 0xaa)
			{
				// found sync
				byte[] tempBytes = new byte[FRAME_SIZE];
				for (int tempByteOffset = 0; tempByteOffset < FRAME_SIZE; tempByteOffset++)
				{
					tempBytes[tempByteOffset] = bytes[byteOffset
							+ tempByteOffset];
				}
				Frame frame = getFrame(tempBytes);
				if (frame != null)
				{
					// it was a valid frame!
					frames.add(frame);
					// skip to next frame -1 as byteOffset will be incremented
					// at the end
					// of the loop block
					byteOffset += FRAME_SIZE - 1;
				} else
				{
					// it wasn't a valid frame, we can skip 2 bytes
					byteOffset++;
				}
			}
			byteOffset++;
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
