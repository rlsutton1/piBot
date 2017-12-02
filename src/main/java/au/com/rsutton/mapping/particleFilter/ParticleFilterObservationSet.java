package au.com.rsutton.mapping.particleFilter;

import java.util.List;

import au.com.rsutton.units.Angle;

public interface ParticleFilterObservationSet
{

	List<ScanObservation> getObservations();

	public Angle getDeadReaconingHeading();

}
