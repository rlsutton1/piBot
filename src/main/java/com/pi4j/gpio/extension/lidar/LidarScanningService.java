package com.pi4j.gpio.extension.lidar;

import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.pi4j.gpio.extension.grovePi.GrovePiProvider;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import au.com.rsutton.config.Config;
import au.com.rsutton.robot.rover.LidarObservation;
import au.com.rsutton.robot.stepper.StepperMotor;

public class LidarScanningService implements Runnable
{

	private LidarScanner scanner;
	private Thread th;
	private static volatile boolean initialised = false;

	private final static Object sync = new Object();
	volatile boolean stop = false;

	// skip 8 steps between scans
	int scanStepSkip = 8;

	public LidarScanningService(GrovePiProvider grove, StepperMotor stepper, Config config)
			throws InterruptedException, IOException, BrokenBarrierException, UnsupportedBusNumberException
	{
		synchronized (sync)
		{
			if (!initialised)
			{
				initialised = true;
				scanner = new LidarScanner(stepper, config, grove);

				th = new Thread(this);
				th.start();
			}
		}
	}

	@Override
	public void run()
	{
		int start = scanner.getMinPosition();
		int end = scanner.getMaxPosition();

		System.out.println("Expect " + (end - start) + " points in a scan");
		while (!stop)
		{
			try
			{
				System.out.println("Start point scan");
				boolean isStart = true;
				for (int a = 0; a < end && !stop; a += scanStepSkip)
				{
					isStart = scanPoint(a, isStart);
				}
				isStart = true;
				for (int a = 0; a > start && !stop; a -= scanStepSkip)
				{
					isStart = scanPoint(a, isStart);
				}
			} catch (Throwable e)
			{
				e.printStackTrace();
			}
		}
	}

	private boolean scanPoint(int a, boolean isStart)
	{
		try
		{

			Vector3D scan = scanner.scan(a);
			if (scan != null)
			{
				publishPoint(scan, isStart);
				isStart = false;
			} else
			{
				System.out.println("Null scan result with sleep");
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (InterruptedException e)
		{
			stop = true;
		}
		return isStart;
	}

	private void publishPoint(Vector3D v, boolean isStart)
	{
		Vector3D temp = new Vector3D(v.getX(), v.getY(), 0);
		LidarObservation lo = new LidarObservation(temp, isStart);

		lo.publish();

	}
}
