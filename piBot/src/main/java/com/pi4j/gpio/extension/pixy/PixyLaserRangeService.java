package com.pi4j.gpio.extension.pixy;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.pi4j.gpio.extension.pixy.PixyCmu5.Frame;

public class PixyLaserRangeService implements Runnable
{
	private PixyLaserRange ranger = new PixyLaserRange();

	private PixyCmu5 pixy;

	AtomicReference<Collection<DistanceVector>> availableData = new AtomicReference<>();

	int[] allowedAngles = null;
	private final static Object sync = new Object();

	public PixyLaserRangeService(int[] allowedAngles) throws IOException
	{
		this.allowedAngles = allowedAngles;
		availableData.set(new LinkedList<DistanceVector>());
		pixy = new PixyCmu5();
		pixy.setup();

		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this,
				10000, 250, TimeUnit.MILLISECONDS);

	}

	public Collection<DistanceVector> getCurrentData()
	{
		synchronized (sync)
		{
			Collection<DistanceVector> data = availableData.get();
			availableData.set(new LinkedList<DistanceVector>());
			return data;
		}
	}

	@Override
	public void run()
	{

		try
		{
			List<Frame> frames = pixy.getFrames();
			Map<Integer, DistanceVector> rangeData = new HashMap<>();

			// System.out.println("pixy frames = " + frames.size());
			for (Frame frame : frames)
			{
				DistanceVector data = ranger.convertFrameToRangeData(frame);
				if (data != null)
				{
					rangeData.put((int) data.angle, data);
				}
			}
			List<DistanceVector> finalData = new LinkedList<>();
			synchronized (sync)
			{
				finalData.addAll(rangeData.values());
				availableData.set(finalData);
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
