package au.com.rsutton.hazelcast;

public class PointCloud  extends MessageBase<PointCloud>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8779513692065087505L;

	PointCloud()
	{
		super(HcTopic.POINT_CLOUD);

	}

}
