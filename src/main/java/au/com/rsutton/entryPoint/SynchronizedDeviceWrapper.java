package au.com.rsutton.entryPoint;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.pi4j.io.i2c.I2CDevice;

public class SynchronizedDeviceWrapper implements I2CDevice
{
	final static Lock lock = new ReentrantLock(true);

	final private I2CDevice device;

	public SynchronizedDeviceWrapper(I2CDevice device)
	{
		try
		{
			lock.lock();
			this.device = device;
		} finally
		{
			lock.unlock();
		}
	}

	@Override
	public void write(byte b) throws IOException
	{
		try
		{
			lock.lock();
			device.write(b);
		} finally
		{
			lock.unlock();
		}
	}

	@Override
	public void write(byte[] buffer, int offset, int size) throws IOException
	{
		try
		{
			lock.lock();
			device.write(buffer, offset, size);
		} finally
		{
			lock.unlock();
		}
	}

	@Override
	public void write(int address, byte b) throws IOException
	{
		try
		{
			lock.lock();
			try
			{
				device.write(address, b);
			} catch (IOException e)
			{
				device.write(address, b);
			}
		} finally
		{
			lock.unlock();
		}
	}

	@Override
	public void write(int address, byte[] buffer, int offset, int size)
			throws IOException
	{
		try
		{
			lock.lock();
			device.write(address, buffer, offset, size);
		} finally
		{
			lock.unlock();
		}
	}

	@Override
	public int read() throws IOException
	{
		try
		{
			lock.lock();
			int ret = device.read();

			return ret;
		} finally
		{
			lock.unlock();
		}
	}

	@Override
	public int read(byte[] buffer, int offset, int size) throws IOException
	{
		try
		{
			lock.lock();
			int ret = device.read(buffer, offset, size);

			return ret;
		} finally
		{
			lock.unlock();
		}
	}

	@Override
	public int read(int address) throws IOException
	{
		try
		{
			lock.lock();
			int ret = device.read(address);

			return ret;
		} finally
		{
			lock.unlock();
		}
	}

	@Override
	public int read(int address, byte[] buffer, int offset, int size)
			throws IOException
	{
		try
		{
			lock.lock();
			int ret = device.read(address, buffer, offset, size);

			return ret;
		} finally
		{
			lock.unlock();
		}
	}

	@Override
	public int read(byte[] writeBuffer, int writeOffset, int writeSize,
			byte[] readBuffer, int readOffset, int readSize) throws IOException
	{
		try
		{
			lock.lock();
			int ret = device.read(writeBuffer,writeOffset,writeSize,readBuffer,readOffset,readSize);

			return ret;
		} finally
		{
			lock.unlock();
		}
	}

}
