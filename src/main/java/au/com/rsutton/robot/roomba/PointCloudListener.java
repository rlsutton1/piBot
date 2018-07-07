package au.com.rsutton.robot.roomba;

import java.util.List;

import org.openni.Point3D;
import org.openni.VideoMode;

public interface PointCloudListener
{

	void evaluatePointCloud(List<Point3D<Float>> pointCloud);

	VideoMode chooseVideoMode(List<VideoMode> supportedModes);

}
