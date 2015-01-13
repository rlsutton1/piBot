package au.com.rsutton.hazelcast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;

import au.com.rsutton.cv.CameraRangeData;
import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.robot.rover.Angle;

import com.google.common.base.Objects;
import com.pi4j.gpio.extension.pixy.DistanceVector;
import com.pi4j.gpio.extension.pixy.Coordinate;

public class RobotLocation extends MessageBase<RobotLocation>
{

	private static final long serialVersionUID = 938950572423708619L;
	private Angle heading;
	private Distance x;
	private Distance y;
	private Speed speed;
	private Distance clearSpaceAhead;
	private CameraRangeData cameraRangeData;
	private long time = System.currentTimeMillis();
	private Angle headingError;

	public RobotLocation()
	{
		super(HcTopic.LOCATION);

	}

	public void setTopic()
	{

		this.topic = HazelCastInstance.getInstance().getTopic(
				HcTopic.LOCATION.toString());
	}

	public void setHeading(Angle angle)
	{
		this.heading = angle;

	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper(RobotLocation.class).add("x", x)
				.add("y", y).add("heading", heading).toString();
	}

	public void setX(Distance distance)
	{
		x = distance;

	}

	public void setY(Distance currentY)
	{
		y = currentY;

	}

	public Distance getX()
	{
		return x;
	}

	public Distance getY()
	{
		return y;
	}

	public Angle getHeading()
	{
		return heading;
	}

	public void setSpeed(Speed speed)
	{
		this.speed = speed;

	}

	public void setClearSpaceAhead(Distance clearSpaceAhead)
	{
		this.clearSpaceAhead = clearSpaceAhead;

	}

	public Distance getClearSpaceAhead()
	{
		return clearSpaceAhead;
	}

	public void setCameraRangeData(CameraRangeData cameraRangeData)
	{
		this.cameraRangeData = cameraRangeData;

	}

	public CameraRangeData getCameraRangeData()
	{
		return this.cameraRangeData;

	}

	public long getTime()
	{
		return time;
	}

	public void setHeadingError(Angle headingError)
	{
		this.headingError = headingError;

	}

	public Angle getHeadingError()
	{
		return headingError;
	}
}
