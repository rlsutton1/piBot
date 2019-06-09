package au.com.rsutton.mapping.particleFilter;

import au.com.rsutton.hazelcast.DataLogLevel;

public enum ParticleFilterStatus
{
	POOR_MATCH(DataLogLevel.WARN), LOCALIZING(DataLogLevel.ERROR), LOCALIZED(DataLogLevel.INFO);

	private final DataLogLevel dataLogLevel;

	ParticleFilterStatus(DataLogLevel level)
	{
		this.dataLogLevel = level;
	}

	public DataLogLevel getDataLogLevel()
	{
		return dataLogLevel;
	}

}
