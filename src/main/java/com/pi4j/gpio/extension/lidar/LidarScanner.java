package com.pi4j.gpio.extension.lidar;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.pi4j.gpio.extension.grovePi.GrovePiPin;
import com.pi4j.gpio.extension.grovePi.GrovePiProvider;
import com.pi4j.io.gpio.PinMode;

import au.com.rsutton.config.Config;
import au.com.rsutton.robot.stepper.StepperMotor;

public class LidarScanner
{

	public static final int MIN_ANGLE = -125;
	public static final int MAX_ANGLE = 125;

	static final double stepsPerRotation = 200;

	static final double microSteps = 8;

	private static final double STEP_ANGLE = 360.0 / (stepsPerRotation * microSteps);

	private Config config;

	// think y =mx+c
	private StepperMotor stepper;
	private Lidar lidar;
	private GrovePiProvider grove;

	public LidarScanner(StepperMotor stepper, Config config, GrovePiProvider grove)
			throws InterruptedException, IOException, BrokenBarrierException
	{

		this.grove = grove;

		grove.setMode(GrovePiPin.GPIO_A2, PinMode.ANALOG_INPUT);

		lidar = new Lidar(config);
		this.stepper = stepper;
		this.config = config;

		init();

	}

	void init() throws InterruptedException, IOException, BrokenBarrierException
	{

		buildPositionRotationMap();

		double ldrMax = 0;

		int maxPos = 0;

		for (int i = 0; i < stepsPerRotation * microSteps; i++)
		{
			stepper.moveTo(i);
			double value = getLdrValue();

			if (value > ldrMax)
			{
				maxPos = i;
			}
			ldrMax = Math.max(ldrMax, value);

		}

		stepper.moveTo((long) (maxPos + (111 * microSteps)));
		stepper.setZero();
		// stepper.moveTo((long) (75 * microSteps));
		// Thread.sleep(1000);

		// double cm = 0;
		// int ctr = 0;
		// while (ctr < 10)
		// {
		//
		// int latestReading = lidar.getLatestReading();
		// if (latestReading != 0)
		// {
		// cm += latestReading;
		// ctr++;
		// }
		// Thread.sleep(10);
		// }
		double c = -34;// - (cm / 100.0);
		lidar.setCalabrationC(c);
		System.out.println("Setting calabration c to " + c);

	}

	private double getLdrValue()
	{
		int samples = 5;
		double v = 0;
		for (int i = 0; i < samples; i++)
		{
			v += grove.getValue(GrovePiPin.GPIO_A2);
		}
		return v / samples;
	}

	double totalSettleTime = 0;
	int samples = 0;

	public Vector3D scanAngle(double angleDegrees) throws InterruptedException, BrokenBarrierException, IOException
	{
		return scan(convertAngleToStepPosition(angleDegrees));

	}

	int getMaxPosition()
	{
		return convertAngleToStepPosition(MAX_ANGLE);
	}

	int getMinPosition()
	{
		return convertAngleToStepPosition(MIN_ANGLE);

	}

	/**
	 * @param position
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public Vector3D scan(int position) throws InterruptedException, IOException
	{
		double angle = convertStepPositionToAnlge(position);
		if (angle > MAX_ANGLE || angle < MIN_ANGLE)
		{
			throw new RuntimeException("Angle " + angle + " out of bounds (position " + position + ")");
		}
		stepper.moveTo(position);

		Thread.sleep(10);
		int last = lidar.getLatestReading();

		Rotation rotation = positionRotationMap.get(position);
		if (rotation == null)
		{
			System.out.println("position not in positionRotationMap " + position);
		}
		return rotation.applyInverseTo(new Vector3D(0, last, 0));
	}

	int convertAngleToStepPosition(double angleDegrees)
	{
		return (int) ((angleDegrees / STEP_ANGLE) + (Math.signum(angleDegrees) * 0.5));
	}

	double convertStepPositionToAnlge(int position)
	{
		return position * STEP_ANGLE;
	}

	Map<Integer, Rotation> positionRotationMap = new HashMap<>();

	private void buildPositionRotationMap()
	{
		int minPosition = convertAngleToStepPosition(MIN_ANGLE);
		int maxPosition = convertAngleToStepPosition(MAX_ANGLE);

		for (int i = minPosition; i <= maxPosition; i++)
		{
			double angle = convertStepPositionToAnlge(i);
			positionRotationMap.put(i, new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(angle)));
		}

	}

}
