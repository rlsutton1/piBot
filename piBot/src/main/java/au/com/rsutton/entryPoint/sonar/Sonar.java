package au.com.rsutton.entryPoint.sonar;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;

import com.pi4j.gpio.extension.adafruit.ChannelListener;

public class Sonar implements ChannelListener {

	private double scalar;
	private int baseOffset;
	volatile private int lastValue;
	private int channel;

	public Sonar(double scalar, int baseOffset, int channel) {
		this.scalar = scalar;
		this.baseOffset = baseOffset;
		this.channel = channel;
	}

	public Distance getCurrentDistance() {
		int distance = lastValue + baseOffset;
		distance *= scalar;

		return new Distance(distance,DistanceUnit.CM);
	}

	@Override
	public void recievedValue(int lastAddress, int i) {

		if (lastAddress == channel)
			lastValue = i;

	}
}
