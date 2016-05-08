package au.com.rsutton.hazelcast;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.Speed;
import au.com.rsutton.mapping.particleFilter.ScanObservation;
import au.com.rsutton.mapping.particleFilter.ParticleFilterObservationSet;
import au.com.rsutton.robot.rover.Angle;
import au.com.rsutton.robot.rover.LidarObservation;

import com.google.common.base.Objects;
import com.pi4j.gpio.extension.lsm303.HeadingData;

public class RobotLocation extends MessageBase<RobotLocation> implements ParticleFilterObservationSet
{

	private static final long serialVersionUID = 938950572423708619L;
	private Angle deadReaconingHeading;
	private Distance x;
	private Distance y;
	private Speed speed;
	private Distance clearSpaceAhead;
	private long time = System.currentTimeMillis();
	private List<LidarObservation> observations;
	private HeadingData compassHeading;

	public RobotLocation()
	{
		super(HcTopic.LOCATION);

	}

	public void setTopic()
	{

		this.topicInstance = HazelCastInstance.getInstance().getTopic(HcTopic.LOCATION.toString());
	}

	public void setDeadReaconingHeading(Angle angle)
	{
		this.deadReaconingHeading = angle;

	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper(RobotLocation.class).add("x", x).add("y", y).add("heading", deadReaconingHeading).toString();
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

	public Angle getDeadReaconingHeading()
	{
		return deadReaconingHeading;
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

	public void addObservations(List<LidarObservation> observations)
	{
		this.observations = observations;

	}

	public List<ScanObservation> getObservations()
	{
	List<ScanObservation>	obs = new LinkedList<>();
	obs.addAll((Collection<? extends ScanObservation>) observations);
		
		return obs;
	}

	public void setObservations(List<LidarObservation> observations)
	{
		this.observations = observations;
	}

	public void setCompassHeading(HeadingData compassData)
	{
		compassHeading = compassData;

	}

	public HeadingData getCompassHeading()
	{
		return compassHeading;
	}
}
