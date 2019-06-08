package au.com.rsutton.hazelcast;

import java.util.LinkedList;
import java.util.List;

import au.com.rsutton.mapping.particleFilter.ScanObservation;
import au.com.rsutton.robot.lidar.LidarObservation;

public class LidarScan extends MessageBase<LidarScan>
{

	private static final long serialVersionUID = 938950572423708619L;

	// time this message was generated
	private long time = System.currentTimeMillis();
	private List<LidarObservation> observations;

	// time that the scan collection started
	private long startTime;

	// time that the scan collection completed
	private long endTime;

	public LidarScan()
	{
		super(HcTopic.LIDAR_SCAN);

	}

	public LidarScan(LidarScan derivedFrom)
	{
		super(HcTopic.LIDAR_SCAN);
		startTime = derivedFrom.startTime;
		endTime = derivedFrom.endTime;
		time = derivedFrom.time;

	}

	public void setTopic()
	{

		this.topicInstance = HazelCastInstance.getInstance().getTopic(HcTopic.LIDAR_SCAN.toString());
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
		List<ScanObservation> obs = new LinkedList<>();
		obs.addAll(observations);

		return obs;
	}

	public void setObservations(List<LidarObservation> observations)
	{
		this.observations = observations;
	}

	public long getStartTime()
	{
		return startTime;
	}

	public void setStartTime(long startTime)
	{
		this.startTime = startTime;
	}

	public long getEndTime()
	{
		return endTime;
	}

	public void setEndTime(long endTime)
	{
		this.endTime = endTime;
	}

}
