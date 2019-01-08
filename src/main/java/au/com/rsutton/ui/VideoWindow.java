package au.com.rsutton.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import au.com.rsutton.hazelcast.ImageMessage;

public class VideoWindow extends JFrame implements MessageListener<ImageMessage>
{

	private static final long serialVersionUID = -4490943128993707547L;

	private AtomicReference<BufferedImage> currentImage = new AtomicReference<>();

	// static public void main(String[] args)
	// {
	// new MapDrawingWindow();
	//
	// }

	volatile boolean stop = false;

	private String registrationId;

	public VideoWindow(String string, int x, int y)
	{
		if (StringUtils.isNotBlank(string))
		{
			setTitle(string);
		}
		this.setBounds(0, 0, 850, 900);

		FlowLayout experimentLayout = new FlowLayout();

		this.setLayout(experimentLayout);

		setSize(700, 700);
		setLocation(x, y);
		setVisible(true);

		JPanel jPanel = new JPanel()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;
				g2.drawImage(currentImage.get(), 0, 0, this);

			}

		};

		jPanel.setPreferredSize(new Dimension(750, 750));

		this.add(jPanel);

		addWindowListener(getWindowCloseListener());

		registrationId = new ImageMessage().addMessageListener(this);

	}

	public VideoWindow(int x, int y)
	{
		this("", x, y);
	}

	public void destroy()
	{
		stop = true;
		dispose();
	}

	private WindowListener getWindowCloseListener()
	{
		return new WindowAdapter()
		{

			@Override
			public void windowClosed(WindowEvent e)
			{
				stop = true;
				new ImageMessage().removeMessageListener(registrationId);

			}

		};
	}

	@Override
	public void onMessage(Message<ImageMessage> message)
	{
		if (!stop)
		{
			try
			{
				currentImage.set(message.getMessageObject().getImage());
				this.repaint();
			} catch (Exception e)
			{
				System.out.println(e);
			}
		}

	}
}
