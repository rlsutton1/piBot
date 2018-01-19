package au.com.rsutton.mapping.particleFilter;

import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.TextField;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import au.com.rsutton.hazelcast.DataLogValue;

public class DataPanel extends JPanel implements MessageListener<DataLogValue>, Runnable
{

	private static final long serialVersionUID = 1L;
	Map<String, String> currentData = new ConcurrentHashMap<>();
	private ScheduledExecutorService pool;

	DataPanel()
	{
		setLayout(new GridLayout(0, 2));

		new DataLogValue().addMessageListener(this);

		pool = Executors.newScheduledThreadPool(1);

		pool.scheduleWithFixedDelay(this, 500, 500, TimeUnit.MILLISECONDS);

	}

	@Override
	public void onMessage(Message<DataLogValue> message)
	{
		currentData.put(message.getMessageObject().getKey(), message.getMessageObject().getValue());

	}

	Map<String, TextField> fields = new HashMap<>();

	@Override
	public void run()
	{

		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				for (Entry<String, String> entry : currentData.entrySet())
				{
					TextField field = fields.get(entry.getKey());
					if (field == null)
					{
						field = new TextField("");

						fields.put(entry.getKey(), field);
						add(new Label(entry.getKey()));
						add(field);
					}
					field.setText(entry.getValue());
					field.setName(entry.getKey());
				}
				validate();
				repaint();

			}
		});

	}
}
