package au.com.rsutton.mapping.particleFilter;

import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.ui.DataSourcePoint;

public interface ParticleFilterIfc
{

	public abstract void addListener(ParticleFilterListener listener);

	public abstract DataSourcePoint getParticlePointSource();

	public abstract DataSourceMap getHeadingMapDataSource();

	public abstract void shutdown();

	public abstract void removeListener(ParticleFilterListener listener);

	void updateMap(ProbabilityMapIIFc map);

}