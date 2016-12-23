package au.com.rsutton.mapping.particleFilter;

public interface ObservationListener
{

	void useScan(ParticleFilterObservationSet observation, Pose pose);

}
