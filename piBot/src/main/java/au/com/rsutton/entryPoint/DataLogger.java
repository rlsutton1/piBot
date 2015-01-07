package au.com.rsutton.entryPoint;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import au.com.rsutton.hazelcast.RobotLocation;

public class DataLogger implements MessageListener<RobotLocation>
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
			fout = new FileOutputStream("robotFlightRecord.obj");
			oos = new ObjectOutputStream(fout);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		RobotLocation locationMessage = new RobotLocation();
		locationMessage.addMessageListener(this);

	}

	@Override
	public void onMessage(Message<RobotLocation> message)
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
