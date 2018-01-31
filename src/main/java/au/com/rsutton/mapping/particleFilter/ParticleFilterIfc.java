package au.com.rsutton.mapping.particleFilter;

import java.util.List;

import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.ui.DataSourcePoint;

public interface ParticleFilterIfc extends RobotPoseSource
{

	@Override
	public abstract double getStdDev();

	public abstract void addListener(ParticleFilterListener listener);

	@Override
	public abstract DataSourcePoint getParticlePointSource();

	@Override
	public abstract DataSourceMap getHeadingMapDataSource();

	@Override
	public abstract Double getBestScanMatchScore();

	public abstract void addPendingScan(ParticleFilterObservationSet par);

	public abstract List<Particle> getParticles();

	@Override
	public abstract Double getBestRawScore();

	@Override
	public abstract void shutdown();

	public abstract void suspend();

	public abstract void resume();

	public abstract void removeListener(ParticleFilterListener listener);
}