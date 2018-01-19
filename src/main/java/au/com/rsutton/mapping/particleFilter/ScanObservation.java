package au.com.rsutton.mapping.particleFilter;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public interface ScanObservation
{

	double getDisctanceCm();

	double getAngleRadians();

	Vector3D getVector();

	int getY();

	int getX();

}
