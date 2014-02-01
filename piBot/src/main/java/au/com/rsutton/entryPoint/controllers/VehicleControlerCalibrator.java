package au.com.rsutton.entryPoint.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import com.pi4j.gpio.extension.adafruit.GyroProvider;

public class VehicleControlerCalibrator {

	private HBridgeController left;
	private HBridgeController right;
	private GyroProvider gyro;

	public VehicleControlerCalibrator(HBridgeController left,
			HBridgeController right, GyroProvider gyro) throws IOException {
		this.left = left;
		this.right = right;
		this.gyro = gyro;

		// loadCalibrationData();
	}

	public void autoConfigure() throws InterruptedException, IOException {
		checkOrientation();
		saveCalibrationData();

	}

	private void checkOrientation() throws InterruptedException {
		// check orientation of the left servo
		int z = gyro.getZ();
		left.setOutput(.80);
		Thread.sleep(1000);
		left.setOutput(0);
		Thread.sleep(500);
		if (gyro.getZ() == z) {
			left.setOutput(0);
			throw new RuntimeException("Didn't move when testing left servo");
		}
		if (gyro.getZ() - z < 0) {
			left.setDirection(ServoController.REVERSED);
		} else {
			left.setDirection(ServoController.NORMAL);
		}

		// check orientation of the right servo
		z = gyro.getZ();
		right.setOutput(.80);
		Thread.sleep(1000);
		right.setOutput(0);
		Thread.sleep(500);
		if (gyro.getZ() == z) {
			right.setOutput(0);
			throw new RuntimeException("Didn't move when testing right servo");
		}
		if (gyro.getZ() - z > 0) {
			right.setDirection(ServoController.REVERSED);
		} else {
			right.setDirection(ServoController.NORMAL);
		}

		System.out.println("right " + right.getDirection());
		System.out.println("left " + left.getDirection());

		// now to prove we have it right.

		// forward without gyro
		System.out.println("Forward");
		right.setOutput(.50);
		left.setOutput(.50);
		Thread.sleep(1000);
		right.setOutput(0);
		left.setOutput(0);

		Thread.sleep(1000);
		
		// back without gyro
		System.out.println("Back");
		right.setOutput(-.50);
		left.setOutput(-.50);
		Thread.sleep(1000);
		right.setOutput(0);
		left.setOutput(0);
	}

	void saveCalibrationData() throws IOException {
		Properties props = new Properties();
		props.put("leftDirection", "" + left.getDirection());
		props.put("rightDirection", "" + right.getDirection());

		OutputStream stream = null;
		try {
			stream = new FileOutputStream(new File("GyroServoController.props"));
			props.store(stream, "GyroServoController config");
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}

}
