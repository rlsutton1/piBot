package au.com.rsutton.robot.roomba;

import java.awt.image.BufferedImage;
import java.util.List;

import org.openni.Point3D;
import org.openni.VideoMode;

public interface PointCloudListener
{

	void evaluatePointCloud(List<Point3D<Float>> pointCloud);

	VideoMode chooseDepthMode(List<VideoMode> supportedModes);

	void evaluateColorFrame(BufferedImage res);

	VideoMode chooseColorMode(List<VideoMode> supportedColorModes);

}
