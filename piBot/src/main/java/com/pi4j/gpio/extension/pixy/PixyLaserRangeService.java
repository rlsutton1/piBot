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

public class PixyLaserRangeService implements Runnable
{
	private PixyLaserRange ranger = new PixyLaserRange();

	private PixyCmu5 pixy;

	AtomicReference<Collection<PixyCoordinate>> availableData = new AtomicReference<>();

	int[] allowedAngles = null;

	volatile private int referenceDistance;
	private final static Object sync = new Object();

	public PixyLaserRangeService(int[] allowedAngles) throws IOException
	{
		this.allowedAngles = allowedAngles;
		availableData.set(new LinkedList<PixyCoordinate>());
		pixy = new PixyCmu5();
		pixy.setup();

		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this,
				10000, 250, TimeUnit.MILLISECONDS);

	}

	public Collection<PixyCoordinate> getCurrentData(int referenceDistance)
	{
		synchronized (sync)
		{
			this.referenceDistance = referenceDistance;
			Collection<PixyCoordinate> data = availableData.get();
			availableData.set(new LinkedList<PixyCoordinate>());
			return data;
		}
	}

	@Override
	public void run()
	{

		try
		{
			List<Frame> frames = pixy.getFrames();
			System.out.println("Got " + frames.size());
			List<PixyCoordinate> coords = new LinkedList<PixyCoordinate>();

			// System.out.println("pixy frames = " + frames.size());
			for (Frame frame : frames)
			{
				PixyCoordinate coord = new PixyCoordinate();
				if (frame.yCenter > PixyLaserRange.Y_CENTER && frame.height > 0)
				{
					coord.x = frame.xCenter;
					coord.y = frame.yCenter;
					coord.count = 1;
					boolean found = false;
					for (PixyCoordinate knownCoord : coords)
					{
						if (coord.x > knownCoord.getAverageX() - 10
								&& coord.x < knownCoord.getAverageX() + 10)
						{
							knownCoord.y += coord.y;
							knownCoord.x += coord.x;
							knownCoord.count++;
							found = true;
							break;
						}
					}
					if (!found)
					{
						coords.add(coord);
					}

				}
			}
			
			List<PixyCoordinate> result = new LinkedList<PixyCoordinate>();
			
			for (PixyCoordinate coord : coords)
			{
				if (coord.count>1)
				{
					result.add(coord);
				}
			}


			synchronized (sync)
			{
				availableData.set(result);
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void dumpCalabrationData()
	{
		for (DistanceVector data : history.values())
		{
			System.out.println(data);

		}
	}

	private void gatherCalabrationData(DistanceVector dv)
	{
		// calabration data collection code
		DistanceVector lastData = history.get(dv);
		if (lastData != null)
		{
			// smooth old data with new data
			lastData.distance = lastData.distance * 0.7 + dv.distance * 0.3;
			lastData.yAngle = lastData.yAngle * 0.7 + dv.yAngle * 0.3;
			lastData.ydistance = lastData.ydistance * 0.7 + dv.ydistance * 0.3;
		} else
		{
			history.put(dv, dv);
		}
	}

	Map<DistanceVector, DistanceVector> history = new HashMap<>();
}
