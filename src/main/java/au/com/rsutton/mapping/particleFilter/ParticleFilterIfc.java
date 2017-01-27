package au.com.rsutton.mapping.particleFilter;

import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.ui.DataSourcePoint;

public interface ParticleFilterIfc
{

	public abstract double getAverageHeading();

	public abstract double getStdDev();

	public abstract void addListener(ParticleFilterListener listener);

	public abstract DataSourcePoint getParticlePointSource();

	public abstract DataSourceMap getHeadingMapDataSource();

	public abstract Double getBestScanMatchScore();

	public abstract Vector3D dumpAveragePosition();

	public abstract void setParticleCount(int max);

	public abstract void addPendingScan(ParticleFilterObservationSet par);

	public abstract List<Particle> getParticles();

	public abstract Double getBestRawScore();

	public abstract void shutdown();
}