package au.com.rsutton.mapping.particleFilter;

import au.com.rsutton.units.Pose;

interface ScanReference
{

	ParticleFilterObservationSet getScan();

	Pose getScanOrigin();

}
