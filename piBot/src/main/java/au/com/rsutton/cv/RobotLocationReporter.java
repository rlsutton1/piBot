package au.com.rsutton.cv;

import java.io.IOException;
import java.util.Map;

public interface RobotLocationReporter
{
	public void report(CameraRangeData cameraRangeData) throws IOException;
	
}
