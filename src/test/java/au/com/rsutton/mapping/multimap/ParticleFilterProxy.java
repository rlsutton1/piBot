package au.com.rsutton.mapping.multimap;

import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.mapping.particleFilter.Particle;
import au.com.rsutton.mapping.particleFilter.ParticleFilterIfc;
import au.com.rsutton.mapping.particleFilter.ParticleFilterListener;
import au.com.rsutton.mapping.particleFilter.ParticleFilterObservationSet;
import au.com.rsutton.mapping.particleFilter.ParticleUpdate;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.ui.DataSourcePoint;

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
	public double getAverageHeading()
	{
		return pf.getAverageHeading();
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
	public Vector3D dumpAveragePosition()
	{
		return pf.dumpAveragePosition();
	}

	@Override
	public void moveParticles(ParticleUpdate particleUpdate)
	{
		pf.moveParticles(particleUpdate);
	}

	@Override
	public void addObservation(ProbabilityMapIIFc currentWorld, ParticleFilterObservationSet observations,
			double compassAdjustment)
	{
		pf.addObservation(currentWorld, observations, compassAdjustment);
	}

	@Override
	public void setParticleCount(int max)
	{
		pf.setParticleCount(max);
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

}
