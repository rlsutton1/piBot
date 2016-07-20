package au.com.rsutton.entryPoint;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.hazelcast.RobotLocation;

import com.hazelcast.core.Message;

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
			fout = new FileInputStream("robotFlightRecord-gyro2.obj");
			oos = new ObjectInputStream(fout);
			boolean canContinue = true;

			double lastHeading = 0;

			while (canContinue == true && stop == false)
			{
				RobotLocation locationMessage = (RobotLocation) ((Message) oos.readObject()).getMessageObject();
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
				// eventOffset *= 5;
				long delay = eventOffset - (System.currentTimeMillis() - referenceTime);
				if (delay > 10)
				{
					Thread.sleep(delay);
				}

				// absolute delay between events
				// Thread.sleep(1000);

				double change = HeadingHelper.getChangeInHeading(lastHeading, locationMessage.getDeadReaconingHeading()
						.getDegrees());
				System.out.println("'heading'," + change + "," + locationMessage.getTime());

				lastHeading = locationMessage.getDeadReaconingHeading().getDegrees();

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
