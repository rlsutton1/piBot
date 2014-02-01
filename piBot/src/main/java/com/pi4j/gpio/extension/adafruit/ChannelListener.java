package com.pi4j.gpio.extension.adafruit;

public interface ChannelListener {

	void recievedValue(int lastAddress, int i);

}
