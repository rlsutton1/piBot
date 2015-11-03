package com.pi4j.gpio.extension.lidar;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public interface LidarDataListener
{

	void addLidarData(Vector3D vector, double distanceCm, double angleDegrees);

}
