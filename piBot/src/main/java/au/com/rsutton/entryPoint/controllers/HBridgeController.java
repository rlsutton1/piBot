package au.com.rsutton.entryPoint.controllers;

import com.pi4j.gpio.extension.adafruit.PwmPin;

public class HBridgeController {

	private PwmPin pwmPin;

	public static final int NORMAL = 1;
	public static final int REVERSED = -1;

	private double direction;

	private int pwmRange = 4095;

	private PwmPin directionPin;

	public HBridgeController(PwmPin pwmPin, PwmPin directionPin, int direction) {
		this.pwmPin = pwmPin;
		this.directionPin = directionPin;
		this.direction = direction;
		pwmPin.setPwmValue(0);
	}

	public void setOutput(double percent) {
		if (percent > 1 || percent < -1) {
			throw new RuntimeException("Cant set HBridge to -100<x>100");
		}

		int pwm = (int) (pwmRange * (Math.abs(percent)));
		int dir = 0;
		if ((percent * direction) > 0) {
			dir = 4095;
		}
		directionPin.setPwmValue(dir);
		pwmPin.setPwmValue(pwm);
//		System.out.println("Setting HBridge to PWM:" + pwm + ", DIR:" + dir
//				+ " for percentage " + percent + " pin " + pwmPin.toString());

	}

	public void setDirection(int direction) {
		this.direction = direction;

	}

	public int getDirection() {

		return (int) direction;
	}

}
