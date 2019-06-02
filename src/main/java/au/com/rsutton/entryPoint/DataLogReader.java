package au.com.rsutton.entryPoint;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import com.hazelcast.core.Message;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.hazelcast.DataLogValue;
import au.com.rsutton.hazelcast.ImageMessage;
import au.com.rsutton.hazelcast.PointCloudMessage;
import au.com.rsutton.hazelcast.RobotTelemetry;

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
			fout = new FileInputStream("robotFlightRecord-2019-01-28-10-50-10.obj");
			oos = new ObjectInputStream(fout);
			boolean canContinue = true;

			double lastHeading = 0;

			while (canContinue == true && stop == false)
			{
				Message<?> messageObject = (Message<?>) oos.readObject();
				messageTime = messageObject.getPublishTime();
				if (messageReferenceTime == 0)
				{
					messageReferenceTime = messageTime;
				}
				long eventOffset = messageTime - messageReferenceTime;
				eventOffset *= 1.0;
				long delay = eventOffset - (System.currentTimeMillis() - referenceTime);
				if (delay > 10)
				{
					Thread.sleep(delay);
				}

				if (messageObject.getMessageObject() instanceof RobotTelemetry)
				{
					RobotTelemetry locationMessage = (RobotTelemetry) messageObject.getMessageObject();
					if (locationMessage == null)
					{
						canContinue = false;
						break;
					}

					double change = HeadingHelper.getChangeInHeading(lastHeading,
							locationMessage.getDeadReaconingHeading().getDegrees());
					System.out.println("'heading'," + change + "," + locationMessage.getTime());

					lastHeading = locationMessage.getDeadReaconingHeading().getDegrees();

					locationMessage.setTime(System.currentTimeMillis());

					locationMessage.setTopic();
					locationMessage.publish();
				}

				if (messageObject.getMessageObject() instanceof DataLogValue)
				{
					DataLogValue locationMessage = (DataLogValue) messageObject.getMessageObject();
					if (locationMessage == null)
					{
						canContinue = false;
						break;
					}

					locationMessage.setTime(System.currentTimeMillis());

					locationMessage.publish();
				}

				if (messageObject.getMessageObject() instanceof PointCloudMessage)
				{
					PointCloudMessage locationMessage = (PointCloudMessage) messageObject.getMessageObject();
					if (locationMessage == null)
					{
						canContinue = false;
						break;
					}

					locationMessage.setTime(System.currentTimeMillis());
					locationMessage.setTopic();
					locationMessage.publish();
				}

				if (messageObject.getMessageObject() instanceof ImageMessage)
				{
					ImageMessage locationMessage = (ImageMessage) messageObject.getMessageObject();
					if (locationMessage == null)
					{
						canContinue = false;
						break;
					}

					locationMessage.setTime(System.currentTimeMillis());
					locationMessage.setTopic();

					locationMessage.publish();
				}

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
