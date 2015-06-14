package com.pi4j.gpio.extension.grovePi;

import java.util.EnumSet;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.impl.PinImpl;

public class GrovePiPin
{
	public static final Pin GPIO_D2 = createDigitalPin(16, "GPIO D2",
			EnumSet.of(PinMode.DIGITAL_INPUT, PinMode.DIGITAL_OUTPUT));

	public static final Pin GPIO_D3 = createDigitalPin(3, "GPIO D3",
			EnumSet.of(PinMode.PWM_OUTPUT, PinMode.DIGITAL_INPUT,
					PinMode.DIGITAL_OUTPUT));

	public static final Pin GPIO_D4 = createDigitalPin(4, "GPIO D4",
			EnumSet.of(PinMode.DIGITAL_INPUT, PinMode.DIGITAL_OUTPUT));

	public static final Pin GPIO_D5 = createDigitalPin(5, "GPIO D5",
			EnumSet.of(PinMode.PWM_OUTPUT, PinMode.DIGITAL_INPUT,
					PinMode.DIGITAL_OUTPUT));

	public static final Pin GPIO_D6 = createDigitalPin(6, "GPIO D6",
			EnumSet.of(PinMode.PWM_OUTPUT, PinMode.DIGITAL_INPUT,
					PinMode.DIGITAL_OUTPUT));

	public static final Pin GPIO_D7 = createDigitalPin(7, "GPIO D7",
			EnumSet.of(PinMode.DIGITAL_INPUT, PinMode.DIGITAL_OUTPUT));

	public static final Pin GPIO_D8 = createDigitalPin(8, "GPIO D8",
			EnumSet.of(PinMode.DIGITAL_INPUT, PinMode.DIGITAL_OUTPUT));

	public static final Pin GPIO_A0 = createDigitalPin(0, "GPIO A0",
			EnumSet.of(PinMode.ANALOG_INPUT, PinMode.DIGITAL_OUTPUT));

	public static final Pin GPIO_A1 = createDigitalPin(1, "GPIO A1",
			EnumSet.of(PinMode.ANALOG_INPUT, PinMode.DIGITAL_OUTPUT));

	public static final Pin GPIO_A2 = createDigitalPin(2, "GPIO A2",
			EnumSet.of(PinMode.ANALOG_INPUT, PinMode.DIGITAL_OUTPUT));

	private static Pin createDigitalPin(int address, String name,
			EnumSet<PinMode> modes)
	{

		return new PinImpl(GrovePiProvider.NAME, address, name, modes, null);
	}
}
