package au.com.rsutton.mapping.particleFilter;

import java.util.List;

import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.ui.DataSourcePoint;
import au.com.rsutton.ui.MapDrawingWindow;

public interface ParticleFilterIfc extends RobotPoseSource
{

	public abstract double getStdDev();

	public abstract void addListener(ParticleFilterListener listener);

	public abstract DataSourcePoint getParticlePointSource();

	public abstract DataSourceMap getHeadingMapDataSource();

	public abstract Double getBestScanMatchScore();

	public abstract void addPendingScan(ParticleFilterObservationSet par);

	public abstract List<Particle> getParticles();

	public abstract Double getBestRawScore();

	public abstract void shutdown();

	public abstract void suspend();

	public abstract void resume();

	public abstract void addDataSoures(MapDrawingWindow ui);
}