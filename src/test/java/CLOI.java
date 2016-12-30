import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.google.common.base.Stopwatch;

public class CLOI
{

	@Test
	public void test1() throws IOException
	{
		String testCommand = "smu1 sweep -10 0.01 10 d";

		int portNumber = 8888;

		String hostName = "192.168.0.116";

		try (Socket kkSocket = new Socket(hostName, portNumber);
				PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));)
		{
			Stopwatch timer = Stopwatch.createStarted();
			for (int i = 0; i < 1; i++)
			{
				out.println(testCommand);
				int fromServer;
				while ((fromServer = in.read()) != -1)
				{
					char c = (char) fromServer;
					System.out.print( c);
					if (c == ']')
					{
						break;
					}
				}
				System.out.println();
			}
			System.out.println(timer.elapsed(TimeUnit.MILLISECONDS)/100);

		}
	}
}
