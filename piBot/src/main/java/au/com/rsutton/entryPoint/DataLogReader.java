package au.com.rsutton.entryPoint;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import com.hazelcast.core.Message;

import au.com.rsutton.hazelcast.RobotLocation;

public class DataLogReader
{

	public static void main(String[] args)
	{
		new DataLogReader().runReader();

	}

	private ObjectInputStream oos;
	private FileInputStream fout;
	volatile private boolean stop = false;

	public DataLogReader()
	{

	}

	public void runReader()
	{
		long referenceTime = System.currentTimeMillis();
		long messageTime = 0;
		long messageReferenceTime = 0;
		try
		{
			fout = new FileInputStream("robotFlightRecord.obj");
			oos = new ObjectInputStream(fout);
			boolean canContinue = true;
			while (canContinue == true && stop == false)
			{
				RobotLocation locationMessage = (RobotLocation) ((Message) oos
						.readObject()).getMessageObject();
				if (locationMessage == null)
				{
					canContinue = false;
					break;
				}
				messageTime = locationMessage.getTime();
				if (messageReferenceTime == 0)
				{
					messageReferenceTime = messageTime;
				}
				long eventOffset = messageTime - messageReferenceTime;
				eventOffset *= 5;
				long delay = eventOffset
						- (System.currentTimeMillis() - referenceTime);
				if (delay > 10)
				{
					Thread.sleep(delay);
				}
				
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
