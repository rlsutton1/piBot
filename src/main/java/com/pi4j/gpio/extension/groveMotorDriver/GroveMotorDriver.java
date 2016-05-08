package com.pi4j.gpio.extension.groveMotorDriver;

import java.io.IOException;

import au.com.rsutton.entryPoint.SynchronizedDeviceWrapper;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

public class GroveMotorDriver
{

	private static final byte MotorSpeedSet = (byte) 0x82;
	private static final byte PWMFrequenceSet = (byte) 0x84;
	private static final byte DirectionSet = (byte) 0xaa;
	private static final byte MotorSetA = (byte) 0xa1;
	private static final byte MotorSetB = (byte) 0xa5;
	private static final byte Nothing = (byte) 0x01;
	private static final byte EnableStepper = (byte) 0x1a;
	private static final byte UnenableStepper = (byte) 0x1b;
	private static final byte Stepernu = (byte) 0x1c;
	private static final byte I2CMotorDriverAdd = (byte) 0x0f; // Set the
																// address of
																// the
	// I2CMotorDriver
	private I2CDevice device;

	public GroveMotorDriver() throws InterruptedException, IOException
	{

		// Get I2C bus
		I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends onthe
															// RasPI version

		// Get the device itself
		device = new SynchronizedDeviceWrapper(bus.getDevice(I2CMotorDriverAdd));

	}

	void stepperrun() throws IOException, InterruptedException
	{
		setNumberOfSteps((byte) 10);
		stepperMotorEnable((byte) 1, (byte) 1);// ennable the i2c motor driver a
												// stepper.
		Thread.sleep(1000);
		stepperMotorDisable();
	}

	// set the steps you want, if 255, the stepper will rotate continuely;
	public void setNumberOfSteps(int numberOfSteps) throws IOException
	{
		byte buffer[] = new byte[] {
				Stepernu, (byte) numberOfSteps, Nothing };

		managedIO(buffer, null);

	}

	// /////////////////////////////////////////////////////////////////////////////
	// Enanble the i2c motor driver to drive a 4-wire stepper. the i2c motor
	// driver will
	// driver a 4-wire with 8 polarity .
	// Direction: stepper direction ; 1/0
	// motor speed: defines the time interval the i2C motor driver change it
	// output to drive the stepper
	// the actul interval time is : motorspeed * 4ms. that is , when motor speed
	// is 10, the interval time
	// would be 40 ms
	// ////////////////////////////////////////////////////////////////////////////////
	public void stepperMotorEnable(int direction, int motorspeed)
			throws IOException
	{
		byte buffer[] = new byte[] {
				EnableStepper, (byte) direction, (byte) motorspeed };

		managedIO(buffer, null);
		// device.write(EnableStepper, buffer, 0, buffer.length);

	}

	// function to uneanble i2C motor drive to drive the stepper.
	public void stepperMotorDisable() throws IOException
	{
		byte buffer[] = new byte[] {
				UnenableStepper, Nothing, Nothing };

		managedIO(buffer, null);
		// device.write(UnenableStepper, buffer, 0, buffer.length);
	}

	// ////////////////////////////////////////////////////////////////////
	// Function to set the 2 DC motor speed
	// motorSpeedA : the DC motor A speed; should be 0~100;
	// motorSpeedB: the DC motor B speed; should be 0~100;

	public void MotorSpeedSetAB(int MotorSpeedA, int MotorSpeedB)
			throws IOException
	{
		MotorSpeedA = (MotorSpeedA);
		MotorSpeedB = (MotorSpeedB);

		byte buffer[] = new byte[] {
				MotorSpeedSet, (byte) MotorSpeedA, (byte) MotorSpeedB };

		// device.write(MotorSpeedSet, buffer, 0, buffer.length);
		managedIO(buffer, null);
	}

	// set the prescale frequency of PWM, 0x03 default;
	public void MotorPWMFrequenceSet(int frequency) throws IOException
	{
		byte buffer[] = new byte[] {
				PWMFrequenceSet, (byte) frequency, Nothing };

		managedIO(buffer, null);
	}

	// set the direction of DC motor.
	void MotorDirectionSet(byte Direction) throws IOException
	{ // Adjust the direction of the motors 0b0000 I4 I3 I2 I1

		byte buffer[] = new byte[2];
		buffer[0] = Direction;
		buffer[1] = Nothing;

		device.write(DirectionSet, buffer, 0, buffer.length);

	}

	void MotorDriectionAndSpeedSet(byte Direction, byte MotorSpeedA,
			byte MotorSpeedB) throws IOException
	{ // you can adjust the driection and speed together
		MotorDirectionSet(Direction);
		MotorSpeedSetAB(MotorSpeedA, MotorSpeedB);
	}

	void managedIO(byte[] write, byte[] read) throws IOException
	{

		for (int i = 0; i < write.length; i++)
		{
			System.out.print(" "+write[i]);
		}
		System.out.println();
		device.write(write, 0, write.length);
	}

}
