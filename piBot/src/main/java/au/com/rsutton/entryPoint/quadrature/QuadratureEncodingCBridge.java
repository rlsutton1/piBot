package au.com.rsutton.entryPoint.quadrature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class QuadratureEncodingCBridge implements QuadratureProvider, Runnable
{

	final private OutputStream outputStream;
	final private InputStream is;
	volatile private long current;

	public QuadratureEncodingCBridge(Pin a, Pin b, boolean invertDirection)
			throws IOException
	{
		String cmdarray[] = new String[3];
		cmdarray[0] = "./c-quadrature";
		if (invertDirection)
		{
			cmdarray[1] = "" + a.getAddress();
			cmdarray[2] = "" + b.getAddress();
		} else
		{
			cmdarray[2] = "" + a.getAddress();
			cmdarray[1] = "" + b.getAddress();

		}
		Process p = Runtime.getRuntime().exec(cmdarray);
		outputStream = p.getOutputStream();
		is = p.getInputStream();
		InputStream stderr = p.getErrorStream();
		InputStreamReader isr = new InputStreamReader(stderr);

		new Thread(this).start();

	}

	@Override
	public long getValue() throws IOException
	{
		// ask for the encoder values
		outputStream.write("A".getBytes());
		outputStream.flush();

		return current;
	}

	@Override
	public void run()
	{
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line = null;
		try
		{
			while ((line = br.readLine()) != null)
			{
				current = Long.parseLong(line.trim());
			}
		} catch (NumberFormatException | IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
