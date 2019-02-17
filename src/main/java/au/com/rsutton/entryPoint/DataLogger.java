package au.com.rsutton.entryPoint;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.hazelcast.core.Message;

import au.com.rsutton.hazelcast.ImageMessage;
import au.com.rsutton.hazelcast.PointCloudMessage;
import au.com.rsutton.hazelcast.RobotLocation;

public class DataLogger
{

	public static void main(String[] args)
	{
		new DataLogger();

	}

	private ObjectOutputStream oos;
	private FileOutputStream fout;

	DataLogger()
	{
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
			fout = new FileOutputStream("robotFlightRecord-" + sdf.format(new Date()) + ".obj");
			oos = new ObjectOutputStream(fout);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		new RobotLocation().addMessageListener(e -> writeObject(e));
		new PointCloudMessage().addMessageListener(e -> writeObject(e));
		new ImageMessage().addMessageListener(e -> writeObject(e));

	}

	private final Object sync = new Object();

	private void writeObject(Message<?> message)
	{
		synchronized (sync)
		{
			try
			{
				oos.writeObject(message);
				oos.flush();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
