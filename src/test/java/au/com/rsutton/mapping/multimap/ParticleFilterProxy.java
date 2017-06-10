package au.com.rsutton.mapping.multimap;

import java.util.List;

import au.com.rsutton.mapping.particleFilter.Particle;
import au.com.rsutton.mapping.particleFilter.ParticleFilterIfc;
import au.com.rsutton.mapping.particleFilter.ParticleFilterListener;
import au.com.rsutton.mapping.particleFilter.ParticleFilterObservationSet;
import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.ui.DataSourcePoint;
import au.com.rsutton.ui.MapDrawingWindow;

public class ParticleFilterProxy implements ParticleFilterIfc
{

	private ParticleFilterIfc pf;

	ParticleFilterProxy(ParticleFilterIfc pf)
	{
		this.pf = pf;
	}

	void changeParticleFilter(ParticleFilterIfc pf)
	{
		this.pf = pf;
	}

	@Override
	public double getHeading()
	{
		return pf.getHeading();
	}

	@Override
	public double getStdDev()
	{
		return pf.getStdDev();
	}

	@Override
	public void addListener(ParticleFilterListener listener)
	{
		pf.addListener(listener);
	}

	@Override
	public DataSourcePoint getParticlePointSource()
	{
		return pf.getParticlePointSource();
	}

	@Override
	public DataSourceMap getHeadingMapDataSource()
	{
		return pf.getHeadingMapDataSource();
	}

	@Override
	public Double getBestScanMatchScore()
	{
		return pf.getBestScanMatchScore();
	}

	@Override
	public DistanceXY getXyPosition()
	{
		return pf.getXyPosition();
	}

	@Override
	public void addPendingScan(ParticleFilterObservationSet par)
	{
		pf.addPendingScan(par);
	}

	@Override
	public List<Particle> getParticles()
	{
		return pf.getParticles();
	}

	@Override
	public Double getBestRawScore()
	{
		return pf.getBestRawScore();
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
	public void addDataSoures(MapDrawingWindow ui)
	{
		pf.addDataSoures(ui);
	}

}
