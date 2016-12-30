package au.com.rsutton.mapping.particleFilter;

import java.util.List;

import com.pi4j.gpio.extension.lsm303.HeadingData;

import au.com.rsutton.robot.rover.Angle;

public interface ParticleFilterObservationSet
{

	HeadingData getCompassHeading();

	List<ScanObservation> getObservations();
	
	public Angle getDeadReaconingHeading();

}
