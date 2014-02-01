package com.pi4j.gpio.extension.adafruit;

public interface GyroListener
{

	void gyroChanged(int outx, int outy, int outz);

}
