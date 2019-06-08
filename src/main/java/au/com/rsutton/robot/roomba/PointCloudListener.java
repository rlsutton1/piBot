package au.com.rsutton.robot.roomba;

import java.awt.image.BufferedImage;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.openni.VideoMode;

public interface PointCloudListener
{

	void evaluatePointCloud(List<Vector3D> pointCloud, long created);

	VideoMode chooseDepthMode(List<VideoMode> supportedModes);

	void evaluateColorFrame(BufferedImage res);

	VideoMode chooseColorMode(List<VideoMode> supportedColorModes);

}
