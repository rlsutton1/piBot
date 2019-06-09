package au.com.rsutton.navigation.router;

import au.com.rsutton.hazelcast.DataLogLevel;

public enum RoutePlannerStatus
{
	STARTUP(DataLogLevel.INFO), BASE_PLANNED(DataLogLevel.WARN), BASE_PLAN_FAILED(DataLogLevel.ERROR), FAILED(
			DataLogLevel.ERROR), SUCCESS(DataLogLevel.INFO), FAILED_TRY_LARGER(DataLogLevel.WARN);

	private final DataLogLevel level;

	RoutePlannerStatus(DataLogLevel level)
	{
		this.level = level;
	}

	public DataLogLevel getLevel()
	{
		return level;
	}

}
