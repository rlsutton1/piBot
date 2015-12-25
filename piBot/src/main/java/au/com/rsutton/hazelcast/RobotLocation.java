package au.com.rsutton.hazelcast;

import java.util.List;
import java.util.Set;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.robot.rover.Angle;
import au.com.rsutton.robot.rover.LidarObservation;

import com.google.common.base.Objects;

public class RobotLocation extends MessageBase<RobotLocation>
{

	private static final long serialVersionUID = 938950572423708619L;
	private Angle heading;
	private Distance x;
	private Distance y;
	private Speed speed;
	private Distance clearSpaceAhead;
	private long time = System.currentTimeMillis();
	private Angle headingError;
	private List<LidarObservation> observations;

	public RobotLocation()
	{
		super(HcTopic.LOCATION);

	}

	public void setTopic()
	{

		this.topicInstance = HazelCastInstance.getInstance().getTopic(
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

	public void addObservations(List<LidarObservation> observations)
	{
		this.observations = observations;
		
	}

	public List<LidarObservation> getObservations()
	{
		return observations;
	}

	public void setObservations(List<LidarObservation> observations)
	{
		this.observations = observations;
	}
}
