package au.com.rsutton.mapping.multimap;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import au.com.rsutton.mapping.particleFilter.ParticleFilterIfc;
import au.com.rsutton.mapping.particleFilter.ParticleFilterListener;
import au.com.rsutton.mapping.particleFilter.ParticleFilterObservationSet;
import au.com.rsutton.mapping.particleFilter.ParticleFilterStatus;
import au.com.rsutton.mapping.particleFilter.ScanObservation;
import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.ui.DataSourcePoint;
import au.com.rsutton.units.Angle;

public class ParticleFilterProxy implements ParticleFilterIfc, ParticleFilterListener
{

	private ParticleFilterIfc pf;
	private List<ParticleFilterListener> listeners = new LinkedList<>();

	public ParticleFilterProxy(ParticleFilterIfc pf)
	{
		this.pf = pf;
		if (pf != null)
		{
			pf.addListener(this);
		}
	}

	public void changeParticleFilter(ParticleFilterIfc pf)
	{
		if (this.pf != null)
		{
			this.pf.shutdown();
			pf.removeListener(this);
		}
		this.pf = pf;
		pf.addListener(this);
	}

	@Override
	public void addListener(ParticleFilterListener listener)
	{
		listeners.add(listener);
	}

	@Override
	public DataSourcePoint getParticlePointSource()
	{
		return new DataSourcePoint()
		{

			@Override
			public List<Point> getOccupiedPoints()
			{
				return pf.getParticlePointSource().getOccupiedPoints();
			}
		};
	}

	@Override
	public DataSourceMap getHeadingMapDataSource()
	{
		return new DataSourceMap()
		{

			@Override
			public List<Point> getPoints()
			{
				return pf.getHeadingMapDataSource().getPoints();
			}

			@Override
			public void drawPoint(BufferedImage image, double pointOriginX, double pointOriginY, double scale,
					double originalX, double originalY)
			{
				pf.getHeadingMapDataSource().drawPoint(image, pointOriginX, pointOriginY, scale, originalX, originalY);

			}
		};

	}

	@Override
	public void addPendingScan(ParticleFilterObservationSet par)
	{
		pf.addPendingScan(par);
	}

	@Override
	public void shutdown()
	{
		pf.shutdown();

	}

	@Override
	public void suspend()
	{
		pf.suspend();
	}

	@Override
	public void resume()
	{
		pf.resume();
	}

	@Override
	public void update(DistanceXY averagePosition, Angle averageHeading, double stdDev,
			List<ScanObservation> particleFilterObservationSet, ParticleFilterStatus status)
	{
		for (ParticleFilterListener listener : listeners)
		{
			listener.update(averagePosition, averageHeading, stdDev, particleFilterObservationSet, status);

		}

	}

	@Override
	public void removeListener(ParticleFilterListener listener)
	{
		listeners.remove(listener);

	}

}
