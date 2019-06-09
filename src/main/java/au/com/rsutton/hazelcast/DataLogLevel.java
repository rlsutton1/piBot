package au.com.rsutton.hazelcast;

import java.awt.Color;

public enum DataLogLevel
{
	INFO(Color.WHITE), WARN(Color.ORANGE), ERROR(Color.RED);

	private final Color color;

	DataLogLevel(Color color)
	{
		this.color = color;
	}

	public Color getColor()
	{
		return color;
	}
}
