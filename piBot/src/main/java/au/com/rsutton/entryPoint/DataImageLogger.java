package au.com.rsutton.entryPoint;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import au.com.rsutton.hazelcast.ImageMessage;
import au.com.rsutton.hazelcast.RobotLocation;

public class DataImageLogger implements MessageListener<ImageMessage>
{

	public static void main(String[] args)
	{
		new DataImageLogger();

	}

	private ObjectOutputStream oos;
	private FileOutputStream fout;

	DataImageLogger()
	{
		try
		{
			fout = new FileOutputStream("robotImageRecord.obj");
			oos = new ObjectOutputStream(fout);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ImageMessage locationMessage = new ImageMessage();
		locationMessage.addMessageListener(this);

	}

	@Override
	public void onMessage(Message<ImageMessage> message)
	{
		try
		{
			System.out.println("Writting message");
			oos.writeObject(message);
			oos.flush();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
