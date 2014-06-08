package com.pi4j.gpio.extension.pixy;

public class Frame
{
	// 0, 1 0 sync (0xaa55)
	// 2, 3 1 checksum (sum of all 16-bit words 2-6)
	// 4, 5 2 signature number
	// 6, 7 3 x center of object
	// 8, 9 4 y center of object
	// 10, 11 5 width of object
	// 12, 13 6 height of object

	int sync = 0;
	int checksum = 0;
	public int signature;
	public int xCenter;
	public int yCenter;
	public int width;
	public int height;
}
