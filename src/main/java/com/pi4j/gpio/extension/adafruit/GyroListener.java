package com.pi4j.gpio.extension.adafruit;

public interface GyroListener
{

	void gyroChanged(double outx, double outy, double outz);

}
