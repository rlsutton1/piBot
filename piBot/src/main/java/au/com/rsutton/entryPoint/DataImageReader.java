package au.com.rsutton.entryPoint;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import com.hazelcast.core.Message;

import au.com.rsutton.hazelcast.ImageMessage;
import au.com.rsutton.hazelcast.RobotLocation;

public class DataImageReader
{

	public static void main(String[] args)
	{
		new DataImageReader().runReader();

	}

	private ObjectInputStream oos;
	private FileInputStream fout;
	volatile private boolean stop = false;

	public DataImageReader()
	{

	}

	public void runReader()
	{
		try
		{
			fout = new FileInputStream("robotImageRecord.obj");
			oos = new ObjectInputStream(fout);
			boolean canContinue = true;
			while (canContinue == true && stop == false)
			{
				ImageMessage locationMessage = (ImageMessage) ((Message) oos
						.readObject()).getMessageObject();
				if (locationMessage == null)
				{
					canContinue = false;
					break;
				}
				
					Thread.sleep(1000);
				
				
				//absolute delay between events
				Thread.sleep(1000);

				locationMessage.setTopic();
				locationMessage.publish();
			}

		} catch (IOException | ClassNotFoundException | InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stop()
	{
		stop = true;

	}

}
