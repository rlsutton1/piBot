package au.com.rsutton.hazelcast;

public class DataLogValue extends MessageBase<DataLogValue>
{

	private static final long serialVersionUID = 938950572423708619L;

	private long time = System.currentTimeMillis();

	String key;
	String value;

	DataLogLevel level;

	public DataLogValue()
	{
		super(HcTopic.DATA_LOG_VALUE);
	}

	public DataLogValue(String key, String value, DataLogLevel level)
	{
		super(HcTopic.DATA_LOG_VALUE);
		this.key = key;
		this.value = value;
		this.level = level;

	}

	public long getTime()
	{
		return time;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public void setTime(long time)
	{
		this.time = time;
	}

	public DataLogLevel getLevel()
	{
		return level;
	}
}
