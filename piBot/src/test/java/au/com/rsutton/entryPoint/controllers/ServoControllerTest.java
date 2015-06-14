package au.com.rsutton.entryPoint.controllers;

import static org.junit.Assert.assertTrue;

import java.util.EnumSet;

import org.junit.Test;

import com.pi4j.gpio.extension.adafruit.PwmPin;
import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.PinListener;

public class ServoControllerTest
{

	Pin rawPin = new Pin()
	{

		@Override
		public String getProvider()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getAddress()
		{
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String getName()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EnumSet<PinMode> getSupportedPinModes()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EnumSet<PinPullResistance> getSupportedPinPullResistance()
		{
			// TODO Auto-generated method stub
			return null;
		}
	};
	private GpioProvider prov = new GpioProvider()
	{

		@Override
		public String getName()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean hasPin(Pin pin)
		{
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void export(Pin pin, PinMode mode)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isExported(Pin pin)
		{
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public PinMode getMode(Pin pin)
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public PinPullResistance getPullResistance(Pin pin)
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public PinState getState(Pin pin)
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public double getValue(Pin pin)
		{
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getPwm(Pin pin)
		{
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void addListener(Pin pin, PinListener listener)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isShutdown()
		{
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void unexport(Pin pin)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setMode(Pin pin, PinMode mode)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setPullResistance(Pin pin, PinPullResistance resistance)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setState(Pin pin, PinState state)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setValue(Pin pin, double value)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setPwm(Pin pin, int value)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeListener(Pin pin, PinListener listener)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeAllListeners()
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void shutdown()
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void export(Pin arg0, PinMode arg1, PinState arg2)
		{
			// TODO Auto-generated method stub
			
		}
	};

	@Test
	public void test()
	{
	
		PwmPin pin = new PwmPin(prov , rawPin);
		ServoController cont = new ServoController(pin , 2048, 4096,ServoController.NORMAL);

		assertTrue("expected 40 got "+cont.getActualPwmOutput(-98),cont.getActualPwmOutput(-98)==40);
		assertTrue("expected 1024 got "+cont.getActualPwmOutput(-50),cont.getActualPwmOutput(-50)==1024);
		assertTrue("expected 2047 got "+cont.getActualPwmOutput(0),cont.getActualPwmOutput(0)==2048);
		assertTrue("expected 3072 got "+cont.getActualPwmOutput(50),cont.getActualPwmOutput(50)==3072);
		assertTrue("expected 4055 got "+cont.getActualPwmOutput(98),cont.getActualPwmOutput(98)==4055);
		
		//cont.resetCenter(-10);
		assertTrue("expected 40 got "+cont.getActualPwmOutput(-98),cont.getActualPwmOutput(-98)==20);
		assertTrue("expected 1023 got "+cont.getActualPwmOutput(-50),cont.getActualPwmOutput(-50)==819);
		assertTrue("expected 1822 got "+cont.getActualPwmOutput(-1),cont.getActualPwmOutput(-1)==1822);
		assertTrue("expected 1843 got "+cont.getActualPwmOutput(0),cont.getActualPwmOutput(0)==1843);
		assertTrue("expected 1863 got "+cont.getActualPwmOutput(1),cont.getActualPwmOutput(1)==1863);
		assertTrue("expected 3072 got "+cont.getActualPwmOutput(50),cont.getActualPwmOutput(50)==2867);
		assertTrue("expected 4055 got "+cont.getActualPwmOutput(98),cont.getActualPwmOutput(98)==3850);
		

	}
}
