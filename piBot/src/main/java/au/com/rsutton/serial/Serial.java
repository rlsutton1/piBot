package au.com.rsutton.serial;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Serial
{

	static public void main(String[] args)
	{
		try
		{
			File file = new File("/dev/ttyUSB0");
			InputStream stream = new FileInputStream(file);
			while (true)
			{

				System.out.print((char) stream.read());

			}
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
