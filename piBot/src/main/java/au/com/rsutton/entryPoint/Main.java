package au.com.rsutton.entryPoint;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.wiringpi.SoftPwm;
import com.pi4j.wiringpi.Spi;

public class Main
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		System.out.println("<--Pi4J--> GPIO Control Example ... started.");

		// create gpio controller
		final GpioController gpio = GpioFactory.getInstance();

		System.out.println("Configure primary gpio out");
		// start pwm on (GPIO_01 / GPIO_18) ...I'm confused - pin 12 of the
		// header // 50%

		// create soft-pwm pins (min=0 ; max=100)
		//SoftPwm.softPwmCreate(1, 2, 4);
		// GpioPinPwmOutput pwmPin =
		 gpio.provisionPwmOutputPin(RaspiPin.GPIO_01, 512);

		// PWM15 - pin to listen for the pwm counter to over flow - pin 12 on
		// the header
		GpioPinDigitalInput counterOverflowPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04, PinPullResistance.PULL_DOWN);

		// BLANK - pin to reset the pwm counter - actuall pin on the header is
		// 18
		final GpioPinDigitalOutput resetCounterPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05, PinState.LOW);

		Thread t = new Thread(new Runnable()
		{

			public void run()
			{
				while (true)
				{
					try
					{
						resetCounterPin.high();
						//Thread.sleep(2);
						resetCounterPin.low();
						Thread.sleep(2);
					} catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		t.setDaemon(true);
		t.start();

		// // create a gpio control trigger on the input pin ; when the input
		// goes
		// // HIGH, also set gpio pin #04 to HIGH
		// counterOverflowPin.addListener(new GpioPinListenerDigital()
		// {
		//
		// int pulseCount = 0;
		//
		// public void
		// handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent
		// event)
		// {
		//
		// // System.out.println("State change " + event.getState());
		// // pin is set to pull up, pwm sinks current holding pin LOW. pin
		// // goes high when the pwm stops sinking current
		// if (event.getState() == PinState.LOW)
		// {
		// resetCounterPin.high();
		// if (pulseCount % 25 == 0)
		// {
		// System.out.println("pulse : " + pulseCount);
		// }
		// pulseCount++;
		//
		// pulseCount %= 10000;
		//
		// } else
		// {
		// try
		// {
		// Thread.sleep(3);
		// } catch (InterruptedException e)
		// {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// resetCounterPin.low();
		// }
		//
		// }
		// });

		// XLAT - trigger 9540 to latch the data received. - pin 22 on the
		// header
		GpioPinDigitalOutput dataLatchPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06);

		try
		{

			System.out.println("Set up wiring spi");
			Spi.wiringPiSPISetup(1, 100000);

			byte[] buffer = null;
			Nibbler nibbler = new Nibbler();

			// set the roll over value to 100 cycles
			nibbler.setPinPwmPercentage(0, 0x11);
			nibbler.setPinPwmPercentage(1, 0x11);
			nibbler.setPinPwmPercentage(2, 65); // <- this is important
			nibbler.setPinPwmPercentage(3, 0x11);
			nibbler.setPinPwmPercentage(4, 0x11);
			nibbler.setPinPwmPercentage(5, 0x11);
			nibbler.setPinPwmPercentage(6, 0x11);
			nibbler.setPinPwmPercentage(7, 0x11);
			nibbler.setPinPwmPercentage(8, 0x11);
			nibbler.setPinPwmPercentage(9, 0x11);
			nibbler.setPinPwmPercentage(10, 0x11);
			nibbler.setPinPwmPercentage(11, 0x11);
			nibbler.setPinPwmPercentage(12, 0x11);
			nibbler.setPinPwmPercentage(13, 0x11);
			nibbler.setPinPwmPercentage(14, 0x11);

//			nibbler.setPinPwmPercentage(15, 0x11);

			String asciiMap = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";

			for (int i = 0; i < 4000; i++)
			{
				System.out.println("Send values to tlc5940 (i=" + i + ")");
				nibbler.setPinPwmPercentage(3, i);
			//	nibbler.setPinPwmPercentage(4, i);
				//nibbler.setPinPwmPercentage(5, i);
				buffer = nibbler.getBytes();

				String buffer2 = new String(buffer);
//				for (byte b : buffer)
//				{
//					buffer2 += b;
//					if (b < 0)
//						b += 256;
//					if (b >= asciiMap.length())
//						b = asciiMap.length() - 1;
//
//					buffer2 += asciiMap.substring(b,b+1);
//				}

				System.out.println("b2 length " + buffer.length );
				for (Byte b: buffer)
				{
					System.out.print(String.format("%02X ", b));
				}
				System.out.println("");

				Spi.wiringPiSPIDataRW(1, buffer2, buffer.length);
				System.out.println("Send latch");
				dataLatchPin.pulse(10, PinState.HIGH, true);
				Thread.sleep(300);

				if (i == 0)
				{
					System.out.println("pulse reset");
					resetCounterPin.pulse(1000, PinState.LOW);
					Thread.sleep(1000);
					System.out.println("pulse reset");
					resetCounterPin.pulse(1000, PinState.HIGH);
					resetCounterPin.pulse(1000, PinState.LOW);
				}
			}
			System.out.println("Exiting...");

		} catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
