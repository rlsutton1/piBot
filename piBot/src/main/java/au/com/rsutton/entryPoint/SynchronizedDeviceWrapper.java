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
		lock.lock();
		this.device = device;
		lock.unlock();
	}

	@Override
	public void write(byte b) throws IOException
	{
		lock.lock();
		device.write(b);
		lock.unlock();

	}

	@Override
	public void write(byte[] buffer, int offset, int size) throws IOException
	{
		lock.lock();
		device.write(buffer, offset, size);
		lock.unlock();
	}

	@Override
	public void write(int address, byte b) throws IOException
	{
		lock.lock();
		try
		{
			device.write(address, b);
		} catch (IOException e)
		{
			device.write(address, b);
		}
		lock.unlock();
	}

	@Override
	public void write(int address, byte[] buffer, int offset, int size)
			throws IOException
	{
		lock.lock();
		device.write(address, buffer, offset, size);
		lock.unlock();
	}

	@Override
	public int read() throws IOException
	{
		lock.lock();
		int ret = device.read();
		lock.unlock();
		return ret;
	}

	@Override
	public int read(byte[] buffer, int offset, int size) throws IOException
	{
		lock.lock();
		int ret = device.read(buffer, offset, size);
		lock.unlock();
		return ret;
	}

	@Override
	public int read(int address) throws IOException
	{
		lock.lock();
		int ret = device.read(address);
		lock.unlock();
		return ret;
	}

	@Override
	public int read(int address, byte[] buffer, int offset, int size)
			throws IOException
	{
		lock.lock();
		int ret = device.read(address, buffer, offset, size);
		lock.unlock();
		return ret;
	}

}
